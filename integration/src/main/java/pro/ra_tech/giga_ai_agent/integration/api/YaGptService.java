package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AskModelResponse;

public interface YaGptService {
    Either<AppFailure, AskModelResponse> askModel(String prompt);
}
