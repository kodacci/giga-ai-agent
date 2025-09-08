package pro.ra_tech.giga_ai_agent.integration.config.llm_text_processor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.LlmTextProcessorServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.api.LlmTextProcessorApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(LlmTextProcessorProps.class)
public class LlmTextProcessorConfig extends BaseIntegrationConfig {
    private static final String LLM_TEXT_PROCESSOR_SERVICE = "llm-text-processor";
    private static final String SPLIT_TEXT_METHOD = "split-text";

    @Bean
    LlmTextProcessorService llmTextProcessorService(LlmTextProcessorProps props, MeterRegistry registry) {
        val client = new OkHttpClient.Builder()
                .callTimeout(props.requestTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();

        val api = new Retrofit.Builder()
                .baseUrl(props.baseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build()
                .create(LlmTextProcessorApi.class);

        val timer = buildTimer(registry, LLM_TEXT_PROCESSOR_SERVICE, SPLIT_TEXT_METHOD);
        val status4xxCounter = buildCounter(registry, ErrorCounterType.STATUS_4XX, LLM_TEXT_PROCESSOR_SERVICE, SPLIT_TEXT_METHOD);
        val status5xxCounter = buildCounter(registry, ErrorCounterType.STATUS_5XX, LLM_TEXT_PROCESSOR_SERVICE, SPLIT_TEXT_METHOD);

        return new LlmTextProcessorServiceImpl(api, timer, status4xxCounter, status5xxCounter, props.maxRetries());
    }
}
