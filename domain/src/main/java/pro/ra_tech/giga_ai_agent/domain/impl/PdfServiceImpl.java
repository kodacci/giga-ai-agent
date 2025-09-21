package pro.ra_tech.giga_ai_agent.domain.impl;

import io.micrometer.core.annotation.Timed;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateDocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.domain.model.DocumentData;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfServiceImpl implements PdfService {
    private final LlmTextProcessorService llmService;
    private final EmbeddingService embeddingService;
    private final HfsService hfsService;
    private final KafkaService kafkaService;
    private final HfsProps hfsProps;
    private final DecimalFormat format = new DecimalFormat("###,###,###");
    private final DocProcessingTaskRepository taskRepo;
    private final SourceRepository sourceRepo;
    private final TagServiceImpl tagsService;
    private final Transactional trx;

    private AppFailure toFailure(Throwable cause) {
        return new DocumentProcessingFailure(
                DocumentProcessingFailure.Code.PDF_PROCESSING_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    @Timed(
            value = "business.process.call",
            extraTags = {"business.process.service", "pdf-service", "business.process.method", "handle-pdf"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, PdfProcessingInfo> handlePdf(
            byte[] contents,
            List<String> tags,
            String name,
            String description
    ) {
        return toText(contents)
                .peek(text -> log.info("Got text with length {} from pdf", format.format(text.length())))
                .flatMap(llmService::splitText)
                .peek(chunks -> log.info("Got {} chunks from llm text processor", chunks.size()))
                .map(chunks -> new DocumentData(name, description, tags, chunks))
                .flatMap(embeddingService::createEmbeddings)
                .map(PdfProcessingInfo::new);
    }

    private record SendResultHandler(
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
                    "Error enqueueing document {} to Kafka, tass {}, error: {}",
                    documentName,
                    taskId,
                    cause
            );
        }
    }

    private CreateSourceData toCreateSourceData(InputDocumentMetadata meta, List<TagData> tags, String hfsId) {
        return new CreateSourceData(
                meta.name(),
                meta.description(),
                tags.stream().map(TagData::id).toList(),
                hfsId
        );
    }

    private Either<AppFailure, Tuple2<Long, Long>> createSourceAndTask(InputDocumentMetadata meta, String hfsId) {
        return trx.execute(status ->
                tagsService.mergeAndSave(meta.tags())
                        .flatMap(tags -> sourceRepo.create(toCreateSourceData(meta, tags, hfsId)))
                        .flatMap(
                                source -> taskRepo.create(new CreateDocProcessingTaskData(source.id(), hfsId))
                                        .map(taskId -> Tuple.of(taskId, source.id()))
                        )
        );
    }

    @Override
    @Timed(
            value = "business.process.call",
            extraTags = {"business.process.service", "pdf-service", "business.process.method", "enqueue-pdf"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, EnqueueDocumentInfo> enqueuePdf(
            byte[] contents,
            InputDocumentMetadata meta
    ) {
        val hfsId = UUID.randomUUID().toString();

        return hfsService.uploadFile(hfsProps.baseFolder(), hfsId, contents)
                .flatMap(nothing -> hfsService.comment(hfsProps.baseFolder(), hfsId, meta.name()))
                .peek(nothing -> log.info("Saved document {} to HFS with id {}", meta.name(), hfsId))
                .flatMap(nothing -> createSourceAndTask(meta, hfsId))
                .peek(taskId -> log.info("Created task {} for document {} processing", taskId, meta.name()))
                .flatMap(taskAndSource -> kafkaService.enqueueDocumentProcessing(
                        new DocumentProcessingTask(taskAndSource._1(), taskAndSource._2(), hfsId, DocumentType.PDF),
                        new SendResultHandler(meta.name(), taskAndSource._1())
                    ).map(nothing -> taskAndSource._2())
                )
                .peek(taskId -> log.info("Enqueued document {} with task {}", meta.name(), taskId))
                .flatMap(taskId ->
                        taskRepo.updateTaskStatus(taskId, DocProcessingTaskStatus.ENQUEUED).map(count -> taskId)
                )
                .map(taskId -> new EnqueueDocumentInfo(taskId, hfsId));
    }

    @Override
    public Either<AppFailure, List<String>> splitToChunks(byte[] contents) {
        return toText(contents).flatMap(llmService::splitText);
    }

    private Either<AppFailure, String> toText(byte[] contents) {
        return Try.withResources(() -> Loader.loadPDF(contents))
                .of(document -> {
                    val stripper = new PDFTextStripper();
                    return stripper.getText(document);
                })
                .toEither()
                .mapLeft(this::toFailure);
    }
}
