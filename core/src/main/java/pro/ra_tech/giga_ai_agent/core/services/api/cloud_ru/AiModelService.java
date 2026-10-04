package pro.ra_tech.giga_ai_agent.core.services.api.cloud_ru;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface AiModelService {
    Either<AppFailure, CreateEmbeddingResponse> createEmbedding(CreateEmbeddingRequest request);
}
