package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateDocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.sql.Types;

@Repository
@RequiredArgsConstructor
public class DocProcessingRepositoryImpl implements DocProcessingTaskRepository {
    private final JdbcClient client;

    private AppFailure toFailure(Throwable cause) {
        return new DatabaseFailure(
                DatabaseFailure.Code.DOC_PROCESSING_TASK_REPOSITORY_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    public Either<AppFailure, DocProcessingTaskData> findById(long id) {
        return Try.of(
                () -> client.sql(
                        "SELECT id, hfs_doc_id, status, chunks_count, processed_chunks_count, source_id created_at" +
                                "FROM doc_processing_tasks " +
                                "WHERE id = :id"
                )
                        .param("id", id)
                        .query(DocProcessingTaskData.class)
                        .single()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "doc-processing-task", "repository.method", "create-task"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, Long> create(CreateDocProcessingTaskData data) {
        return Try.of(
                () -> client.sql(
                            "INSERT INTO doc_processing_tasks (hfs_doc_id, status, source_id) " +
                                    "VALUES (:hfsDocId, 'IDLE', :sourceId) RETURNING id"
                        )
                        .param("hfsDocId", data.hfsDocId())
                        .param("sourceId", data.sourceId())
                        .query(Long.class)
                        .single()
        )
                .toEither()
                .mapLeft(this::toFailure);

    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "doc-processing-task", "repository.method", "update-task-status"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, Integer> updateTaskStatus(long id, DocProcessingTaskStatus status) {
        return Try.of(
                () -> client.sql(
                        "UPDATE doc_processing_tasks SET status = :status WHERE id = :id"
                )
                        .param("status", status, Types.OTHER)
                        .param("id", id)
                        .update()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, Integer> updateTaskProgress(long id, int processedChunks) {
        return Try.of(
                () -> client.sql(
                        "UPDATE doc_processing_tasks (processed_chunks_count = :processedChunks) WHERE id = :id"
                )
                        .param("processedChunks", processedChunks)
                        .param("id", id)
                        .update()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }
}
