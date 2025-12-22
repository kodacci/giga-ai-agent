package pro.ra_tech.giga_ai_agent.core.services.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.core.controllers.sources.dto.ListSourcesResponse;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface SourcesService {
    Either<AppFailure, ListSourcesResponse> listSources();
}
