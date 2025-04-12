package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.api.LlmTextProcessorApi;
import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.model.SplitTextRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.model.SplitTextResponse;
import retrofit2.Response;

import java.util.List;

@RequiredArgsConstructor
public class LlmTextProcessorServiceImpl extends BaseRestService implements LlmTextProcessorService {
    private final LlmTextProcessorApi api;
    private final Timer splitTextTimer;
    private final RetryPolicy<Response<SplitTextResponse>> splitTextRetryPolicy;
    private final Counter status4xxCounter;
    private final Counter status5xxCounter;

    public LlmTextProcessorServiceImpl(
            LlmTextProcessorApi api,
            Timer splitTextTimer,
            Counter status4xxCounter,
            Counter status5xxCounter,
            int maxRetries
    ) {
        this.api = api;
        this.splitTextTimer = splitTextTimer;
        this.status4xxCounter = status4xxCounter;
        this.status5xxCounter = status5xxCounter;

        splitTextRetryPolicy = buildPolicy(maxRetries);
    }

    private AppFailure toFailure(Throwable cause) {
        return toFailure(IntegrationFailure.Code.LLM_TEXT_PROCESSOR_FAILURE, getClass().getName(), cause);
    }

    @Override
    public Either<AppFailure, List<String>> splitText(String text) {
        return sendMeteredRequest(
                splitTextRetryPolicy,
                splitTextTimer,
                status4xxCounter,
                status5xxCounter,
                api.splitText(new SplitTextRequest(text)),
                this::toFailure
        )
                .map(SplitTextResponse::chunks);
    }
}
