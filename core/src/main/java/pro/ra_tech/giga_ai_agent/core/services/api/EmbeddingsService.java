package pro.ra_tech.giga_ai_agent.core.services.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto.RecalculateResponse;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface EmbeddingsService {
    Either<AppFailure, RecalculateResponse> enqueueRecalculation(long sourceId);
}
