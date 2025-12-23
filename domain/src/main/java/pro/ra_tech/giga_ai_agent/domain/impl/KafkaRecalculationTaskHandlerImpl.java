package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskStatus;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingsRecalculationService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.RecalculationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaRecalculationTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.impl.BaseRestService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;

@Slf4j
@RequiredArgsConstructor
public class KafkaRecalculationTaskHandlerImpl implements KafkaRecalculationTaskHandler {
    public static class TaskErrorException extends RuntimeException {
        public TaskErrorException(long taskId) {
            super("Task " + taskId + " in error status, skipping recalculating embedding");
        }
    }

    private final EmbeddingsRecalculationTaskRepository taskRepo;
    private final EmbeddingsRecalculationService recalculationService;


    private AppFailure toFailure(Throwable cause) {
        return new RecalculationFailure(
                RecalculationFailure.Code.EMBEDDINGS_RECALCULATION_TASK_ERROR_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private Either<AppFailure, Void> handleEmbeddingApiError(AppFailure failure) {
            val cause = failure.getCause();
            if (cause instanceof BaseRestService.RestApiException &&
                    ((BaseRestService.RestApiException) cause).getHttpCode() == 413) {
                log.warn("Too many tokens in create embedding request, skipping embedding...", failure.getCause());

                return Either.right(null);
            }

            return Either.left(failure);
    }

    @Override
    public void onEmbeddingRecalculation(EmbeddingRecalculationTask task) {
        log.info("Recalculating embedding, task: {}", task);
        taskRepo.findById(task.taskId())
                .flatMap(data -> data.status() == RecalculationTaskStatus.ERROR
                        ? Either.left(toFailure(new TaskErrorException(data.id())))
                        : Either.right(data)
                )
                .flatMap(data -> recalculationService.recalculateEmbedding(task.embeddingId()))
                .fold(this::handleEmbeddingApiError, Either::<AppFailure, Void>right)
                .flatMap(nothing -> taskRepo.incrementTaskProgress(task.taskId()))
                .peek(progress -> log.info("Successfully recalculated embedding for task {}, progress {}", task.taskId(), progress * 100.0 / task.embeddingsCount()))
                .flatMap(progress -> progress == task.embeddingsCount()
                        ? taskRepo.updateStatus(task.taskId(), RecalculationTaskStatus.SUCCESS)
                            .peek(nothing -> log.info("Successfully finished embeddings recalculation task {}", task.taskId()))
                        : Either.right(null)
                )
                .peekLeft(failure -> log.error("Error recalculating embeddings for task {}", task.taskId(), failure.getCause()))
                .peekLeft(failure -> taskRepo.updateStatus(task.taskId(), RecalculationTaskStatus.ERROR)
                        .peekLeft(ff -> log.error("Error setting embeddings recalculation task {} status to ERROR", task.taskId(), ff.getCause()))
                )
        ;
    }
}
