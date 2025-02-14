package pro.ra_tech.giga_ai_agent.integration.config.telegram;

import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.impl.TelegramBotServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.impl.TelegramBotUpdatesHandler;
import pro.ra_tech.giga_ai_agent.integration.impl.TelegramListener;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.api.TelegramBotApi;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(TelegramBotProps.class)
@ComponentScan("pro.ra_tech.giga_ai_agent.integration.impl")
public class TelegramBotConfig {
    private static final int SEC_TO_MS = 1000;

    private TelegramBotApi api(
            TelegramBotProps props,
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
    public TelegramBotService telegramBotService(TelegramBotProps props) {
        return new TelegramBotServiceImpl(
                api(props, props.requestTimeoutMs()),
                api(props, props.updateTimeoutSec() * SEC_TO_MS + props.requestTimeoutMs()),
                props.maxRetries(),
                props.updateLimit(),
                props.updateTimeoutSec()
        );
    }

    @Bean
    public BlockingQueue<BotUpdate> botUpdatesQueue(TelegramBotProps props) {
        return new LinkedBlockingDeque<>(props.updatesQueueCapacity());
    }

    @Bean
    public TaskExecutor botListenerExecutor(TelegramListener listener) {
        val executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("telegram-bot-listener-");
        executor.initialize();
        executor.execute(listener);

        return executor;
    }

    @Bean
    public TaskExecutor botUpdateHandlerExecutor(TelegramBotUpdatesHandler handler) {
        val executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("telegram-bot-updates-handler-");
        executor.initialize();
        executor.execute(handler);

        return executor;
    }
}
