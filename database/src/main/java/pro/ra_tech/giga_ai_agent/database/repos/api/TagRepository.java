package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface TagRepository extends WritableRepository<String, TagData> {
    Either<AppFailure, List<TagData>> findByNames(List<String> names);
    Either<AppFailure, List<TagData>> create(List<String> names);
    Either<AppFailure, Boolean> exists(String name);
}
