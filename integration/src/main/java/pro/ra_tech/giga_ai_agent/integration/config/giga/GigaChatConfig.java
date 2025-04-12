package pro.ra_tech.giga_ai_agent.integration.config.giga;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaAuthServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaChatServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AuthResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class GigaChatConfig extends BaseIntegrationConfig {
    private static final String GIGA_CHAT_SERVICE = "giga-chat";
    private static final String GIGA_AUTH_SERVICE = "giga-auth";

    @Bean
    public OkHttpClient gigaHttpClient(GigaChatProps props) {
        return new OkHttpClient.Builder()
                .callTimeout(props.requestTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public ThreadPoolTaskScheduler gigaAuthScheduler() {
        val scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("giga-auth-");
        scheduler.initialize();

        return scheduler;
    }

    @Bean
    public GigaAuthService gigaAuthenticator(
            OkHttpClient gigaHttpClient,
            GigaChatProps props,
            ThreadPoolTaskScheduler gigaAuthScheduler,
            MeterRegistry registry
    ) {
        val authApi = new Retrofit.Builder()
                .baseUrl(props.authApiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(gigaHttpClient)
                .build();

        val retryPolicy = RetryPolicy.<Response<AuthResponse>>builder().withMaxRetries(props.maxRetries()).build();
        val timer = buildTimer(registry, GIGA_AUTH_SERVICE, "authenticate");

        return new GigaAuthServiceImpl(
                props.clientId(),
                props.authKey(),
                retryPolicy,
                authApi.create(AuthApi.class),
                gigaAuthScheduler,
                props.authRetryTimeoutMs(),
                timer,
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_AUTH_SERVICE, "authenticate"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_AUTH_SERVICE, "authenticate")
        );
    }

    @Bean
    public GigaChatService gigaChatService(
            OkHttpClient client,
            GigaChatProps props,
            GigaAuthService authService,
            MeterRegistry registry
    ) {
        val gigaApi = new Retrofit.Builder()
                .baseUrl(props.apiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        return new GigaChatServiceImpl(
                authService,
                gigaApi.create(GigaChatApi.class),
                props.maxRetries(),
                props.retryTimeoutMs(),
                buildTimer(registry, GIGA_CHAT_SERVICE, "get-models"),
                buildTimer(registry, GIGA_CHAT_SERVICE, "chat-completions"),
                buildTimer(registry, GIGA_CHAT_SERVICE, "get-balance"),
                buildTimer(registry, GIGA_CHAT_SERVICE, "create-embeddings"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, "get-models"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, "chat-completions"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, "get-balance"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, "create-embeddings"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, "get-models"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, "chat-completions"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, "get-balance"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, "create-embeddings"),
                props.stubEmbeddings()
        );
    }
}
