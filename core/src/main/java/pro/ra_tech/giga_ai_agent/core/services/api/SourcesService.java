package pro.ra_tech.giga_ai_agent.core.services.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceWithTagsDto;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface SourcesService {
    Either<AppFailure, List<SourceWithTagsDto>> listSources();
}
