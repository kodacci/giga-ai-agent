package pro.ra_tech.giga_ai_agent.integration.config.telegram;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.TelegramBotServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.api.TelegramBotApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class TelegramApiConfig extends BaseIntegrationConfig {
    private static final int SEC_TO_MS = 1000;
    private static final String TELEGRAM_SERVICE = "telegram";

    private TelegramBotApi api(
            TelegramApiProps props,
            int timeoutMs
    ) {
        val client = new OkHttpClient.Builder()
                .callTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        return new Retrofit.Builder()
                .baseUrl(props.restApiBaseUrl() + "/bot" + props.apiToken() + "/")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build()
                .create(TelegramBotApi.class);
    }

    @Bean
    public TelegramBotService telegramBotService(TelegramApiProps props, MeterRegistry registry) {
        return new TelegramBotServiceImpl(
                api(props, props.requestTimeoutMs()),
                api(props, props.updateTimeoutSec() * SEC_TO_MS + props.requestTimeoutMs()),
                props.maxRetries(),
                props.updateLimit(),
                props.updateTimeoutSec(),
                buildTimer(registry, TELEGRAM_SERVICE, "send-message"),
                buildTimer(registry, TELEGRAM_SERVICE, "get-me"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, TELEGRAM_SERVICE, "send-message"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, TELEGRAM_SERVICE, "get-updates"),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, TELEGRAM_SERVICE, "get-me"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, TELEGRAM_SERVICE, "send-message"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, TELEGRAM_SERVICE, "get-updates"),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, TELEGRAM_SERVICE, "get-me")
        );
    }
}
