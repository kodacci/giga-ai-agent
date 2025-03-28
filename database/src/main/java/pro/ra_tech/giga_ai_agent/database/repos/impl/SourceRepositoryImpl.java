package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SourceRepositoryImpl implements SourceRepository {
    private final JdbcClient jdbc;

    private AppFailure toFailure(Throwable throwable) {
        return new DatabaseFailure(
                DatabaseFailure.Code.SOURCE_REPOSITORY_FAILURE,
                getClass().getName(),
                throwable
        );
    }

    private long joinWithTags(long id, List<Long> tags) {
        tags.forEach(
                tag -> jdbc.sql("INSERT INTO sources_tags_join (source_id, tag_id) VALUES (:source_id, :tag_id)")
                        .param("source_id", id)
                        .param("tag_id", tag)
                        .query()
        );

        return id;
    }

    @Override
    public Either<AppFailure, SourceData> create(CreateSourceData data) {
        return Try.of(
                () -> jdbc.sql("INSERT INTO sources (name) VALUES (:name) RETURNING id")
                        .param("name", data.name())
                        .query(Long.class)
                        .single()
        )
                .map(id -> joinWithTags(id, data.tags()))
                .toEither()
                .map(id -> new SourceData(id, data.name(), data.tags()))
                .mapLeft(this::toFailure);
    }
}
