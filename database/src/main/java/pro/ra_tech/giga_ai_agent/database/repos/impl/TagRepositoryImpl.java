package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {
    private final JdbcClient jdbc;

    private AppFailure toFailure(Throwable cause) {
        return new DatabaseFailure(
                DatabaseFailure.Code.TAG_REPOSITORY_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    public Either<AppFailure, List<TagData>> findByNames(List<String> names) {
        return Try.of(
                () -> jdbc.sql("SELECT id, name FROM tags WHERE name in (:names)")
                        .param("names", names)
                        .query(DataClassRowMapper.newInstance(TagData.class))
                        .list()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, List<TagData>> create(List<String> names) {
        return Try.of(
                () -> names.stream().map(this::createUnsafe).toList()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, Boolean> exists(String name) {
        return Try.of(
                () -> !jdbc.sql("SELECT 1 from tags WHERE name = :name")
                        .param(name)
                        .query()
                        .listOfRows()
                        .isEmpty()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    private TagData createUnsafe(String name) {
        return new TagData(
                jdbc.sql("INSERT INTO tags (name) VALUES (:name) RETURNING id")
                        .param("name", name)
                        .query(Long.class)
                        .single(),
                name
        );
    }

    @Override
    public Either<AppFailure, TagData> create(String data) {
        return Try.of(() -> createUnsafe(data))
                .toEither()
                .mapLeft(this::toFailure);
    }
}
