package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.ChunkProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class KafkaDocProcessingTaskHandlerImpl implements KafkaDocProcessingTaskHandler {
    private final String baseFolder;
    private final HfsService hfsService;
    private final PdfService pdfService;
    private final KafkaService kafka;
    private final DocProcessingTaskRepository taskRepo;
    private final EmbeddingService embeddingService;

    public static class TaskErrorStatusException extends RuntimeException {
        public TaskErrorStatusException(long taskId) {
            super("Task " + taskId + " in error status, skipping processing chunk");
        }
    }

    @RequiredArgsConstructor
    private static class KafkaSendResultHandlerImpl implements KafkaSendResultHandler {
        private final int idx;
        private final String docId;

        @Override
        public void handleSuccess() {
            log.info("Successfully enqueued chunk {} for document {}", idx, docId);
        }

        @Override
        public void handleError(Throwable cause) {
            log.info("Error enqueueing chunk {} for document {}", idx, docId);
        }
    }

    private void setErrorStatus(long taskId) {
        taskRepo.updateTaskStatus(taskId, DocProcessingTaskStatus.ERROR)
                .peekLeft(failure -> log.error(
                        "Error setting ERROR task status for {}: {}",
                        taskId,
                        failure.getMessage()
                ));
    }

    private void enqueueChunks(long taskId, long sourceId, String hfsDocId, List<String> chunks) {
        IntStream.range(0, chunks.size())
                .boxed()
                .map(idx -> kafka.enqueueChunkProcessing(
                        new ChunkProcessingTask(taskId, sourceId, hfsDocId, idx, chunks.get(idx), chunks.size()),
                        new KafkaSendResultHandlerImpl(idx, hfsDocId)
                ))
                .filter(Either::isLeft)
                .findAny()
                .ifPresentOrElse(
                        result ->
                            result.peekLeft(
                                    failure -> log.error(
                                            "Error queueing chunk for task {}: {}",
                                            taskId,
                                            failure.getMessage()
                                    )
                            )
                                    .peekLeft(failure -> setErrorStatus(taskId)),
                        () -> log.info("Successfully enqueued {} chunks for task {}", chunks.size(), taskId)
                );
    }

    @Override
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Handling document processing task: {}", task);

        taskRepo.updateTaskStatus(task.taskId(), DocProcessingTaskStatus.STARTED)
                .flatMap(rows -> hfsService.downloadFile(baseFolder, task.hfsDocumentId()))
                .flatMap(pdfService::splitToChunks)
                .peek(chunks -> log.info("Got {} chunks for task {}", chunks.size(), task.taskId()))
                .peek(chunks -> enqueueChunks(task.taskId(), task.sourceId(), task.hfsDocumentId(), chunks))
                .peekLeft(failure -> log.error("Error processing task {}", task.taskId()))
                .peekLeft(failure -> setErrorStatus(task.taskId()));
    }

    private AppFailure toFailure(Throwable cause) {
        return new DocumentProcessingFailure(
                DocumentProcessingFailure.Code.CHUNK_PROCESSING_TASK_ERROR,
                getClass().getName(),
                cause
        );
    }

    @Override
    public void onChunkProcessingTask(ChunkProcessingTask task) {
        log.info(
                "Handling chunk processing task: {}, progress: {}",
                task,
                Math.floor(100.0 * task.chunkIdx() / task.chunksCount())
        );

        taskRepo.findById(task.taskId())
                .flatMap(data -> data.status() == DocProcessingTaskStatus.ERROR
                        ? Either.left(toFailure(new TaskErrorStatusException(task.taskId())))
                        : Either.right(data)
                )
                .flatMap(data -> embeddingService.createEmbedding(task.text(), task.sourceId()))
                .flatMap(nothing -> {
                    if (task.chunkIdx() == task.chunksCount() - 1) {
                        return taskRepo.updateTaskStatus(task.taskId(), DocProcessingTaskStatus.SUCCESS)
                                .peekLeft(failure -> log.error(
                                        "Error setting SUCCESS status to task {}:",
                                        task.taskId(),
                                        failure.getCause()
                                ));
                    }

                    return Either.right(0);
                })
                .fold(
                        failure -> {
                            taskRepo.updateTaskStatus(task.taskId(), DocProcessingTaskStatus.ERROR)
                                            .peekLeft(ff ->
                                                    log.error("Error setting task error status:", ff.getCause())
                                            );
                            log.error("Error processing task {}, chunk {}", task.taskId(), task.chunkIdx());

                            return null;
                        },
                        success -> {
                            log.info(
                                    "Successfully created embedding for task {} for chunk {}", task.taskId(), task.chunkIdx()
                            );

                            return null;
                        }
                );
    }
}
