package pro.ra_tech.giga_ai_agent.integration.config.ya_gpt;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.YaGptAuthServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AuthResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@ConditionalOnProperty(name = "app.ya-gpt.enabled", havingValue = "true")
public class YaGptConfig extends BaseIntegrationConfig {
    private static final String YA_GPT_AUTH_SERVICE = "ya-gpt-auth";
    private static final String YA_GPT_AUTH_AUTHENTICATE = "authenticate";

    @Bean
    public TaskScheduler yaGptAuthScheduler() {
        return buildThreadPoolScheduler(1, "ya-gpt-auth-");
    }

    @Bean
    public OkHttpClient yaGptHttpClient(YaGptProps props) {
        return buildOkHttpClient(props.requestTimeoutMs());
    }

    @Bean
    public YaGptAuthServiceImpl yaGptService(
            OkHttpClient yaGptHttpClient,
            YaGptProps props,
            TaskScheduler yaGptAuthScheduler,
            MeterRegistry registry
    ) {
        val authApi = new Retrofit.Builder()
                .baseUrl(props.authApiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(yaGptHttpClient)
                .build();

        val retryPolicy = RetryPolicy.<Response<AuthResponse>>builder().withMaxRetries(props.maxRetries()).build();
        val timer = buildTimer(registry, YA_GPT_AUTH_SERVICE, YA_GPT_AUTH_AUTHENTICATE);

        return new YaGptAuthServiceImpl(
                authApi.create(AuthApi.class),
                props.oAuthToken(),
                retryPolicy,
                timer,
                buildCounter(registry, ErrorCounterType.STATUS_4XX, YA_GPT_AUTH_SERVICE, YA_GPT_AUTH_AUTHENTICATE),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, YA_GPT_AUTH_SERVICE, YA_GPT_AUTH_AUTHENTICATE),
                yaGptAuthScheduler,
                props.retryTimeoutMs()
        );
    }
}
