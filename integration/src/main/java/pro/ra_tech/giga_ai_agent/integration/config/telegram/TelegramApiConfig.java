package pro.ra_tech.giga_ai_agent.integration.config.telegram;

import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.impl.TelegramBotServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.api.TelegramBotApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(TelegramApiProps.class)
public class TelegramApiConfig {
    private static final int SEC_TO_MS = 1000;

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
    public TelegramBotService telegramBotService(TelegramApiProps props) {
        return new TelegramBotServiceImpl(
                api(props, props.requestTimeoutMs()),
                api(props, props.updateTimeoutSec() * SEC_TO_MS + props.requestTimeoutMs()),
                props.maxRetries(),
                props.updateLimit(),
                props.updateTimeoutSec()
        );
    }
}
