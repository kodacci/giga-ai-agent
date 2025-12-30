package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateDocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.api.TagService;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.config.hfs.HfsProps;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentType;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseDocumentService {
    protected final LlmTextProcessorService llmService;
    protected final EmbeddingService embeddingService;
    protected final HfsService hfsService;
    protected final KafkaService kafkaService;
    protected final HfsProps hfsProps;
    protected final DecimalFormat format = new DecimalFormat("###,###,###");
    protected final DocProcessingTaskRepository taskRepo;
    protected final SourceRepository sourceRepo;
    protected final TagService tagsService;
    protected final Transactional trx;

    public record SendResultHandler(
            String documentName,
            long taskId
    ) implements KafkaSendResultHandler {
        @Override
        public void handleSuccess() {
            log.info("Successfully enqueued document {} to Kafka, task {}", documentName, taskId);
        }

        @Override
        public void handleError(Throwable cause) {
            log.error(
                    "Error enqueueing document {} to Kafka, task {}, error: {}",
                    documentName,
                    taskId,
                    cause
            );
        }
    }

    protected AppFailure toFailure(DocumentProcessingFailure.Code code, Throwable cause) {
        return new DocumentProcessingFailure(code, getClass().getName(), cause);
    }

    protected CreateSourceData toCreateSourceData(InputDocumentMetadata meta, List<TagData> tags, String hfsId) {
        return new CreateSourceData(
                meta.name(),
                meta.description(),
                tags.stream().map(TagData::id).toList(),
                hfsId
        );
    }

    protected Either<AppFailure, Tuple2<Long, Long>> createSourceAndTask(InputDocumentMetadata meta, String hfsId) {
        return trx.execute(status ->
                tagsService.mergeAndSave(meta.tags())
                        .flatMap(tags -> sourceRepo.create(toCreateSourceData(meta, tags, hfsId)))
                        .flatMap(
                                source -> taskRepo.create(new CreateDocProcessingTaskData(source.id(), hfsId))
                                        .map(taskId -> Tuple.of(taskId, source.id()))
                        )
        );
    }

    protected Either<AppFailure, EnqueueDocumentInfo> enqueue(
            byte[] contents,
            InputDocumentMetadata meta,
            DocumentType type
    ) {
        val hfsId = UUID.randomUUID().toString();

        return hfsService.uploadFile(hfsProps.baseFolder(), hfsId, contents)
                .flatMap(nothing -> hfsService.comment(hfsProps.baseFolder(), hfsId, meta.name()))
                .peek(nothing -> log.info("Saved document {} to HFS with id {}", meta.name(), hfsId))
                .flatMap(nothing -> createSourceAndTask(meta, hfsId))
                .peek(taskAndSource -> log.info("Created task&source {} for document {} processing", taskAndSource, meta.name()))
                .flatMap(taskAndSource -> kafkaService.enqueueDocumentProcessing(
                                new DocumentProcessingTask(taskAndSource._1(), taskAndSource._2(), hfsId, type),
                                new SendResultHandler(meta.name(), taskAndSource._1())
                        ).map(nothing -> taskAndSource._1())
                )
                .peek(taskId -> log.info("Enqueued document {} with task {}", meta.name(), taskId))
                .flatMap(taskId ->
                        taskRepo.updateTaskStatus(taskId, DocProcessingTaskStatus.ENQUEUED).map(count -> taskId)
                )
                .map(taskId -> new EnqueueDocumentInfo(taskId, hfsId));
    }
}
