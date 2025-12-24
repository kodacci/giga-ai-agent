package pro.ra_tech.giga_ai_agent.integration.config.giga;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.config.kafka.KafkaProps;
import pro.ra_tech.giga_ai_agent.integration.config.telegram.TelegramApiProps;
import pro.ra_tech.giga_ai_agent.integration.config.ya_gpt.YaGptProps;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaAuthServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaChatServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AuthResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
@EnableConfigurationProperties(GigaChatProps.class)
public class GigaChatConfig extends BaseIntegrationConfig {
    private static final String GIGA_CHAT_SERVICE = "giga-chat";
    private static final String GIGA_AUTH_SERVICE = "giga-auth";
    private static final String GIGA_AUTH_AUTHENTICATE = "authenticate";
    private static final String GIGA_CHAT_SERVICE_GET_MODELS = "get-models";
    private static final String GIGA_CHAT_SERVICE_CHAT_COMPLETIONS = "chat-completions";
    private static final String GIGA_CHAT_SERVICE_GET_BALANCE = "get-balance";
    private static final String GIGA_CHAT_SERVICE_CREATE_EMBEDDINGS = "create-embeddings";

    @Bean
    public OkHttpClient gigaHttpClient(GigaChatProps props) {
        return buildOkHttpClient(props.requestTimeoutMs());
    }

    @Bean
    public TaskScheduler gigaAuthScheduler() {
        return buildThreadPoolScheduler(1, "giga-auth-");
    }

    @Bean
    public GigaAuthService gigaAuthenticator(
            OkHttpClient gigaHttpClient,
            GigaChatProps props,
            TaskScheduler gigaAuthScheduler,
            MeterRegistry registry
    ) {
        val authApi = new Retrofit.Builder()
                .baseUrl(props.authApiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(gigaHttpClient)
                .build();

        val retryPolicy = RetryPolicy.<Response<AuthResponse>>builder().withMaxRetries(props.maxRetries()).build();
        val timer = buildTimer(registry, GIGA_AUTH_SERVICE, GIGA_AUTH_AUTHENTICATE);

        return new GigaAuthServiceImpl(
                props.clientId(),
                props.authKey(),
                retryPolicy,
                authApi.create(AuthApi.class),
                gigaAuthScheduler,
                props.authRetryTimeoutMs(),
                timer,
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_AUTH_SERVICE, GIGA_AUTH_AUTHENTICATE),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_AUTH_SERVICE, GIGA_AUTH_AUTHENTICATE)
        );
    }

    @Bean
    public GigaChatService gigaChatService(
            OkHttpClient gigaHttpClient,
            GigaChatProps props,
            GigaAuthService authService,
            MeterRegistry registry
    ) {
        val gigaApi = new Retrofit.Builder()
                .baseUrl(props.apiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(gigaHttpClient)
                .build();

        return new GigaChatServiceImpl(
                authService,
                gigaApi.create(GigaChatApi.class),
                props.maxRetries(),
                props.retryTimeoutMs(),
                buildTimer(registry, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_GET_MODELS),
                buildTimer(registry, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_CHAT_COMPLETIONS),
                buildTimer(registry, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_GET_BALANCE),
                buildTimer(registry, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_CREATE_EMBEDDINGS),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_GET_MODELS),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_CHAT_COMPLETIONS),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_GET_BALANCE),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_CREATE_EMBEDDINGS),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_GET_MODELS),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_CHAT_COMPLETIONS),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_GET_BALANCE),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, GIGA_CHAT_SERVICE, GIGA_CHAT_SERVICE_CREATE_EMBEDDINGS),
                props.stubEmbeddings()
        );
    }
}
