package pro.ra_tech.giga_ai_agent.core.services.api.giga;

import io.vavr.control.Either;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.GetAiModelsResponse;
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
