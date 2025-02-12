package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.rest.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiAskMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAskRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelType;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiRole;
import pro.ra_tech.giga_ai_agent.integration.rest.model.GetAiModelsResponse;
import retrofit2.Response;

import java.util.List;

@Slf4j
public class GigaChatServiceImpl extends BaseService implements GigaChatService {
    private final GigaAuthService authService;
    private final GigaChatApi gigaApi;
    private final RetryPolicy<Response<GetAiModelsResponse>> getAiModelsPolicy;
    private final RetryPolicy<Response<AiModelAnswerResponse>> askAiModelPolicy;

    public GigaChatServiceImpl(
            GigaAuthService authService,
            GigaChatApi gigaApi,
            int maxRetries
    ) {
        this.authService = authService;
        this.gigaApi = gigaApi;

        getAiModelsPolicy = buildPolicy(maxRetries);
        askAiModelPolicy = buildPolicy(maxRetries);

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
    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "giga-chat", "integration.method", "get-models"},
            histogram = true,
            percentiles ={0.9, 0.95, 0.99}
    )
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
    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "giga-chat", "integration.method", "chat-completions"},
            histogram = true,
            percentiles ={0.9, 0.95, 0.99}
    )
    public Either<AppFailure, AiModelAnswerResponse> askModel(
            String rqUid,
            AiModelType model,
            String prompt,
            @Nullable String sessionId
    ) {
        log.info("Asking model {} with prompt {}", model, prompt);
        val request = AiModelAskRequest.builder()
                .model(model)
                .messages(List.of(
                        new AiAskMessage(
                                AiRole.USER,
                                prompt,
                                null,
                                null
                        )
                ))
                .build();

        log.info("Sending chat completions request {}", request);
        return authService.getAuthHeader()
                .flatMap(
                        authHeader -> sendRequest(
                                askAiModelPolicy,
                                gigaApi.askModel(
                                        authHeader,
                                        authService.getClientId(),
                                        rqUid,
                                        sessionId,
                                        request
                                ),
                                this::toFailure
                        )
                )
                .peek(res -> log.info("Successfully got model response on {}: {}", rqUid, res))
                .peekLeft(failure -> log.error("Error asking model {}: ", model, failure.getCause()));
    }
}
