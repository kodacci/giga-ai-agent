package pro.ra_tech.giga_ai_agent.database.repos.impl;

import com.pgvector.PGvector;
import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingPersistentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmbeddingRepositoryImpl implements EmbeddingRepository {
    private final JdbcClient client;

    private AppFailure toFailure(Throwable cause) {
        return new DatabaseFailure(
                DatabaseFailure.Code.EMBEDDINGS_REPOSITORY_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private long insert(CreateEmbeddingData data) {
        return client.sql(
                        "INSERT INTO embeddings (source_id, text_data, vector_data) " +
                                "VALUES (:source_id, :text_data, :vector_data) RETURNING id"
                )
                .param("source_id", data.sourceId())
                .param("text_data", data.textData())
                .param("vector_data", new PGvector(data.vectorData()))
                .query(Long.class)
                .single();
    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "embedding", "repository.method", "create-embeddings"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, List<EmbeddingPersistentData>> createEmbeddings(List<CreateEmbeddingData> data) {
        return Try.of(
                () -> data.stream().map(embedding -> new EmbeddingPersistentData(
                        insert(embedding),
                        embedding.sourceId(),
                        embedding.textData()
                ))
                        .toList()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "embedding", "repository.method", "create-embedding"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, EmbeddingPersistentData> createEmbedding(CreateEmbeddingData data) {
        return Try.of(() -> insert(data))
                .toEither()
                .map(res -> new EmbeddingPersistentData(res, data.sourceId(), data.textData()))
                .mapLeft(this::toFailure);
    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "embedding", "repository.method", "vector-search"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, List<EmbeddingPersistentData>> vectorSearch(List<Double> promptVector) {
        return Try.of(
                () -> client.sql(
                        "SELECT id, source_id as source, text_data " +
                                "FROM embeddings ORDER BY vector_data <-> :search_vector LIMIT 5"
                )
                        .param("search_vector", new PGvector(promptVector))
                        .query(DataClassRowMapper.newInstance(EmbeddingPersistentData.class))
                        .list()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }
}
