package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateRecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingTextData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskStatus;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingsRecalculationService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmbeddingsRecalculationServiceImpl implements EmbeddingsRecalculationService {
    private static final int EMBEDDINGS_LIMIT = 100;

    private final EmbeddingsRecalculationTaskRepository taskRepo;
    private final EmbeddingRepository embeddingRepo;
    private final KafkaService kafkaService;
    private final Transactional trx;

    private record SendResultHandler(
            long chunkIdx,
            long taskId
    ) implements KafkaSendResultHandler {
        @Override
        public void handleSuccess() {
            log.info("Successfully enqueued recalculation chunk {} to Kafka, task {}", chunkIdx, taskId);
        }

        @Override
        public void handleError(Throwable cause) {
            log.error(
                    "Error enqueueing recalculation chunk {} to Kafka, task {}, error: {}",
                    chunkIdx,
                    taskId,
                    cause
            );
        }
    }

    private Either<AppFailure, Void> enqueueEmbeddings(
            long taskId,
            long offset,
            long count,
            long sourceId,
            List<EmbeddingTextData> embeddings
    ) {
        return IntStream.range(0, embeddings.size())
                .boxed()
                .map(idx -> {
                    val embedding = embeddings.get(idx);
                    val chunkIdx = idx + offset;

                    return kafkaService.enqueueEmbeddingRecalculation(
                            new EmbeddingRecalculationTask(
                                    taskId,
                                    sourceId,
                                    idx + offset,
                                    embedding.text(),
                                    count
                            ),
                            new SendResultHandler(chunkIdx, taskId)
                    )
                            .peekLeft(failure -> log.error("Error enqueueing recalculation chunk {} to Kafka, task {}", chunkIdx, taskId, failure.getCause()));
                })
                .filter(Either::isLeft)
                .findAny()
                .orElse(Either.right(null));
    }

    private Either<AppFailure, Long> enqueueAll(long count, long sourceId) {
        return taskRepo.create(new CreateRecalculationTaskData(sourceId, count))
            .flatMap(taskId ->
                    LongStream.iterate(0, i -> i < count, i -> i + EMBEDDINGS_LIMIT)
                            .boxed()
                            .map(i -> embeddingRepo.findBySourceId(sourceId, i, EMBEDDINGS_LIMIT)
                                    .flatMap(embeddings -> enqueueEmbeddings(taskId, i, count, sourceId, embeddings))
                                    .map(data -> taskId)
                            )
                            .filter(Either::isLeft)
                            .findAny()
                            .orElse(Either.right(taskId))
                            .peekLeft(failure -> log.error("Error creating embeddings recalculation task for source {}", sourceId, failure.getCause()))
                            .peekLeft(failure ->
                                    taskRepo.updateStatus(taskId, RecalculationTaskStatus.ERROR)
                                            .peekLeft(ff -> log.error(
                                                    "Error setting embeddings recalculation task {} status to ERROR",
                                                    taskId,
                                                    ff.getCause()
                                            ))
                            )
            );
    }

    @Override
    public Either<AppFailure, Long> enqueueAll(long sourceId) {
        return embeddingRepo.countBySourceId(sourceId)
                .flatMap(count -> enqueueAll(count, sourceId));
    }
}
