package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.rest.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.GetAiModelsResponse;
import retrofit2.Response;

@Slf4j
public class GigaChatServiceImpl extends BaseService implements GigaChatService {
    private final GigaAuthService authService;
    private final GigaChatApi gigaApi;
    private final RetryPolicy<Response<GetAiModelsResponse>> getAiModelsPolicy;

    public GigaChatServiceImpl(
            GigaAuthService authService,
            GigaChatApi gigaApi,
            int maxRetries
    ) {
        this.authService = authService;
        this.gigaApi = gigaApi;

        getAiModelsPolicy = buildPolicy(maxRetries);

        log.info("Created Giga Chat service for client {}", authService.getClientId());
    }

    private static <T> RetryPolicy<Response<T>> buildPolicy(int maxRetries) {
        return RetryPolicy.<Response<T>>builder().withMaxRetries(maxRetries).build();
    }

    private AppFailure toFailure(Throwable cause) {
        return toFailure(
                IntegrationFailure.Code.GIGA_CHAT_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    public Either<AppFailure, GetAiModelsResponse> listModels() {
        log.info("Getting ai models list");

        return authService.getAuthHeader()
                .flatMap(
                        authHeader -> sendRequest(
                                getAiModelsPolicy,
                                gigaApi.getAiModels(authHeader),
                                this::toFailure
                        )
                )
                .peekLeft(failure -> log.error("Error getting models list:", failure.getCause()));
    }

    @Override
    public Either<AppFailure, AiModelAnswerResponse> askModel(String prompt) {
        return null;
    }
}
