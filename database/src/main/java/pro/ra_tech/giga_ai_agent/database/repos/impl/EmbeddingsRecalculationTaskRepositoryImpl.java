package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateRecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskStatus;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.sql.Types;

@Repository
@RequiredArgsConstructor
@Slf4j
public class EmbeddingsRecalculationTaskRepositoryImpl extends BaseRepository implements EmbeddingsRecalculationTaskRepository {
    private final JdbcClient jdbc;

    @Override
    protected DatabaseFailure.Code failureCode() {
        return DatabaseFailure.Code.EMBEDDINGS_RECALCULATION_TASK_REPOSITORY_FAILURE;
    }

    @Override
    public Either<AppFailure, RecalculationTaskData> findById(long id) {
        return Try.of(
                () -> jdbc.sql(
                        "SELECT id, status, embeddings_count, processed_embeddings_count, source_id, created_at " +
                                "FROM embeddings_recalculation_tasks WHERE id = :id"
                        )
                        .param("id", id)
                        .query(RecalculationTaskData.class)
                        .single()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, Long> create(CreateRecalculationTaskData data) {
        return Try.of(
                () -> jdbc.sql(
                        "INSERT INTO embeddings_recalculation_tasks " +
                                "(status, embeddings_count, processed_embeddings_count, source_id) " +
                                "VALUES ('STARTED', :count, 0, :sourceId) RETURNING id"
                )
                        .param("count", data.embeddingsCount())
                        .param("sourceId", data.sourceId())
                        .query(Long.class)
                        .single()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, Void> updateStatus(long id, RecalculationTaskStatus status) {
        return Try.of(
                () -> jdbc.sql(
                        "UPDATE embeddings_recalculation_tasks SET status=:status WHERE id=:id"
                )
                        .param("status", status, Types.OTHER)
                        .param("id", id)
                        .update()
        )
                .toEither()
                .mapLeft(this::toFailure)
                .map(res -> null);
    }
}
