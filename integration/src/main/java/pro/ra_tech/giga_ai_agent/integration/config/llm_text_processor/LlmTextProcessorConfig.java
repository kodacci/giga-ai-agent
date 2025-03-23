package pro.ra_tech.giga_ai_agent.integration.config.llm_text_processor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.impl.LlmTextProcessorServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.api.LlmTextProcessorApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(LlmTextProcessorProps.class)
public class LlmTextProcessorConfig {
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

        val timer = Timer.builder("integration.call")
                .tags("integration.service", "llm-text-processor", "integration.method", "splitText")
                .publishPercentileHistogram()
                .publishPercentiles(0.9, 0.95, 0.99)
                .register(registry);

        return new LlmTextProcessorServiceImpl(api, timer, props.maxRetries());
    }
}
