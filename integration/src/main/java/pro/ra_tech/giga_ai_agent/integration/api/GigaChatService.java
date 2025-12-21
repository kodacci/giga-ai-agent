package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import lombok.Synchronized;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.*;

import java.util.List;

public interface GigaChatService {
    Either<AppFailure, GetAiModelsResponse> listModels();

    Either<AppFailure, AiModelAnswerResponse> askModel(
            String rqUid,
            AiModelType model,
            String prompt,
            @Nullable String sessionId,
            @Nullable List<String> context
    );

    Either<AppFailure, GetBalanceResponse> getBalance(@Nullable String sessionId);
    Either<AppFailure, GetBalanceResponse> getBalance();

    Either<AppFailure, CreateEmbeddingsResponse> createEmbeddings(List<String> input, EmbeddingModel model);
}
