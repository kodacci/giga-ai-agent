package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelType;
import pro.ra_tech.giga_ai_agent.integration.rest.model.GetAiModelsResponse;

public interface GigaChatService {
    Either<AppFailure, GetAiModelsResponse> listModels();
    Either<AppFailure, AiModelAnswerResponse> askModel(
            String rqUid,
            AiModelType model,
            String prompt,
            @Nullable String sessionId
    );
}
