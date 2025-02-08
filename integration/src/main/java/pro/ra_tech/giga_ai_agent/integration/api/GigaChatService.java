package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.GetAiModelsResponse;

import java.util.List;

public interface GigaChatService {
    Either<AppFailure, GetAiModelsResponse> listModels();
    Either<AppFailure, AiModelAnswerResponse> askModel(String prompt);
}
