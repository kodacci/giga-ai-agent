package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceWithTagsDto;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class SourceRepositoryImpl extends BaseRepository implements SourceRepository {
    private final JdbcClient jdbc;

    @Override
    protected DatabaseFailure.Code failureCode() {
        return DatabaseFailure.Code.SOURCE_REPOSITORY_FAILURE;
    }

    private long joinWithTags(long id, List<Long> tags) {
        tags.forEach(
                tag -> jdbc.sql("INSERT INTO sources_tags_join (source_id, tag_id) VALUES (:source_id, :tag_id)")
                        .param("source_id", id)
                        .param("tag_id", tag)
                        .update()
        );

        return id;
    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "source", "repository.method", "create"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, SourceData> create(CreateSourceData data) {
        return Try.of(
                () -> jdbc.sql("INSERT INTO sources (name, description, hfs_doc_id) VALUES (:name, :description, :hfsDocId) RETURNING id")
                        .param("name", data.name())
                        .param("description", data.description())
                        .param("hfsDocId", data.hfsDocId())
                        .query(Long.class)
                        .single()
        )
                .map(id -> joinWithTags(id, data.tags()))
                .toEither()
                .bimap(this::toFailure, id -> new SourceData(id, data.name(), data.tags()));
    }

    @Override
    @Timed(
            value = "repository.call",
            extraTags = {"repository.name", "source", "repository.method", "list"},
            histogram = true,
            percentiles = {0.90, 0.95, 0.99}
    )
    public Either<AppFailure, List<SourceWithTagsDto>> list(long offset, int limit) {
        return Try.of(
                () -> jdbc.sql(
                        "SELECT s.id as id, s.name as name, s.description as description, " +
                                "COALESCE(json_agg(t.name ORDER BY t.id ASC) " +
                                "FILTER (WHERE t.id IS NOT NULL), '[]'::json) as tags, s.hfs_doc_id as \"hfsDocId\" " +
                                "FROM sources s " +
                                "LEFT JOIN sources_tags_join st ON s.id = st.source_id " +
                                "LEFT JOIN tags t ON st.tag_id = t.id " +
                                "GROUP BY s.id ORDER BY s.id ASC limit :limit OFFSET :offset"
                )
                        .param("limit", limit)
                        .param("offset", offset)
                        .query(SourceWithTagsDto.class)
                        .list()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, List<String>> getNames(List<Long> ids) {
        return Try.of(
                () -> jdbc.sql("SELECT name FROM sources WHERE id IN (:ids)")
                        .param("ids", ids)
                        .query(String.class)
                        .list()
        )
                .toEither()
                .mapLeft(this::toFailure);
    }
}
