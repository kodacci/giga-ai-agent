package pro.ra_tech.giga_ai_agent.core.services.api;

import io.vavr.control.Either;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.GetAiModelsResponse;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface AiModelService {
    Either<AppFailure, GetAiModelsResponse> listModels();

    Either<AppFailure, AskAiModelResponse> askModel(
            String rqUid,
            @Nullable String sessionId,
            AskAiModelRequest data
    );

    Either<AppFailure, CreateEmbeddingResponse> createEmbedding(CreateEmbeddingRequest request);
}
