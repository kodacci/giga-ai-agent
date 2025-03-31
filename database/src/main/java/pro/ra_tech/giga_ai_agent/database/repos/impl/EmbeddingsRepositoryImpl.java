package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EmbeddingsRepositoryImpl implements EmbeddingRepository {
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
                .param("vector_data", String.format(
                        "[%s]",
                        data.vectorData().stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(","))
                ))
                .query(Long.class)
                .single();
    }

    @Override
    public Either<AppFailure, List<EmbeddingData>> createEmbeddings(List<CreateEmbeddingData> data) {
        return Try.of(
                () -> data.stream().map(embedding -> new EmbeddingData(
                        insert(embedding),
                        embedding.sourceId(),
                        embedding.textData(),
                        embedding.vectorData()
                ))
                        .toList()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }
}
