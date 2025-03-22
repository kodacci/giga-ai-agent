package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
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

    public LlmTextProcessorServiceImpl(LlmTextProcessorApi api, Timer splitTextTimer, int maxRetries) {
        this.api = api;
        this.splitTextTimer = splitTextTimer;

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
                api.splitText(new SplitTextRequest(text)),
                this::toFailure
        )
                .map(SplitTextResponse::chunks);
    }
}
