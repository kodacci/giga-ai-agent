package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.AuthService;
import pro.ra_tech.giga_ai_agent.integration.api.YaGptService;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.api.YaGptApi;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AskModelRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AskModelResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.ModelMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.ModelRole;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.PromptCompletionOptions;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.ReasoningOptions;
import retrofit2.Response;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class YaGptServiceImpl extends BaseRestService implements YaGptService {
    private final AuthService authService;
    private final String modelUri;
    private final YaGptApi api;
    private final RetryPolicy<Response<AskModelResponse>> askModelRetryPolicy;
    private final Timer askModelTimer;
    private final Counter askModel4xxCounter;
    private final Counter askModel5xxCounter;

    private AppFailure toFailure(Throwable cause) {
        return toFailure(
                IntegrationFailure.Code.YA_GPT_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    public Either<AppFailure, AskModelResponse> askModel(String prompt) {
        log.info("Asking Yandex GPT with prompt: {}", prompt);

        val request = new AskModelRequest(
                modelUri,
                new PromptCompletionOptions(
                        false,
                        0,
                        10000,
                        new ReasoningOptions(ReasoningOptions.Mode.DISABLED)
                ),
                List.of(new ModelMessage(ModelRole.USER, prompt))
        );

        return authService.getAuthHeader()
                .flatMap(auth -> sendMeteredRequest(
                        askModelRetryPolicy,
                        askModelTimer,
                        askModel4xxCounter,
                        askModel5xxCounter,
                        api.askModel(
                                auth,
                                request
                        ),
                        this::toFailure
                ))
                .peek(res -> log.info("Got response from Yandex GPT: {}", res))
                .peekLeft(failure -> log.error("Error getting response from Yandex GPT:", failure.getCause()));
    }
}
