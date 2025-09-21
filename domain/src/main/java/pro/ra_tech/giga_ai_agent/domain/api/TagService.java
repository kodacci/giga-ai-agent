package pro.ra_tech.giga_ai_agent.domain.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface TagService {
    Either<AppFailure, List<TagData>> mergeAndSave(List<String> tags);
}
