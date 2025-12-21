package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.collection.Stream;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateRecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingTextData;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingsRecalculationService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;

import java.util.List;
import java.util.stream.LongStream;

@Slf4j
@RequiredArgsConstructor
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

    private void enqueueEmbeddings(
            long taskId,
            long offset,
            long count,
            long sourceId,
            List<EmbeddingTextData> embeddings
    ) {
        Stream.range(0, embeddings.size())
                .forEach(idx -> {
                    val embedding = embeddings.get(idx);
                    val chunkIdx = idx + offset;

                    kafkaService.enqueueEmbeddingRecalculation(
                            new EmbeddingRecalculationTask(
                                    taskId,
                                    sourceId,
                                    idx + offset,
                                    embedding.text(),
                                    count
                            ),
                            new SendResultHandler(chunkIdx, taskId)
                    );
                });
    }

    private Either<AppFailure, Void> enqueueAll(long count, long sourceId) {
        return trx.execute(status ->
                taskRepo.create(new CreateRecalculationTaskData(sourceId, count))
                    .flatMap(taskId ->
                            LongStream.iterate(0, i -> i < count, i -> i += EMBEDDINGS_LIMIT)
                                    .boxed()
                                    .map(i -> embeddingRepo.findBySourceId(sourceId, i, EMBEDDINGS_LIMIT)
                                            .peek(embeddings -> enqueueEmbeddings(taskId, i, count, sourceId, embeddings))
                                    )
                                    .filter(Either::isLeft)
                                    .findAny()
                                    .orElse(Either.right(List.of()))
                    )

        )
                .map(data -> null);
    }

    @Override
    public Either<AppFailure, Void> enqueueAll(long sourceId) {
        return embeddingRepo.countBySourceId(sourceId)
                .flatMap(count -> enqueueAll(count, sourceId));
    }
}
