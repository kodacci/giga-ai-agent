package pro.ra_tech.giga_ai_agent.domain.config;

import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.impl.EmbeddingServiceImpl;
import pro.ra_tech.giga_ai_agent.domain.impl.TelegramBotUpdatesHandler;
import pro.ra_tech.giga_ai_agent.domain.impl.TelegramListener;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.config.giga.GigaChatProps;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.IntStream;

@Configuration
@EnableConfigurationProperties(TelegramBotProps.class)
@ComponentScan("pro.ra_tech.giga_ai_agent.domain.impl")
public class DomainConfig {
    @Bean
    public BlockingQueue<BotUpdate> botUpdatesQueue(TelegramBotProps props) {
        return new LinkedBlockingDeque<>(props.updatesQueueCapacity());
    }

    @Bean
    @ConditionalOnProperty(value = "app.telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
    public TaskExecutor botListenerExecutor(TelegramListener listener) {
        val executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("telegram-bot-listener-");
        executor.initialize();
        executor.execute(listener);

        return executor;
    }

    @Bean
    @ConditionalOnProperty(value = "app.telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
    public TaskExecutor botUpdateHandlerExecutor(
            TelegramBotProps props,
            BlockingQueue<BotUpdate> botUpdatesQueue,
            TelegramBotService botService,
            GigaChatService gigaChatService,
            EmbeddingRepository embeddingRepository
    ) {
        val executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(props.updatesHandlersCount());
        executor.setMaxPoolSize(props.updatesHandlersCount());
        executor.setThreadNamePrefix("telegram-bot-updates-handler-");
        executor.initialize();

        IntStream.range(0, props.updatesHandlersCount())
                .forEach(idx -> {
                    executor.execute(new TelegramBotUpdatesHandler(
                            botUpdatesQueue,
                            botService,
                            gigaChatService,
                            props.aiModelType(),
                            embeddingRepository
                    ));
                });

        return executor;
    }

    @Bean
    public EmbeddingService embeddingService(
            GigaChatProps gigaProps,
            Transactional trx,
            TagRepository tagRepo,
            SourceRepository sourceRepo,
            EmbeddingRepository embeddingRepo,
            GigaChatService chatService
    ) {
        return new EmbeddingServiceImpl(
                trx,
                tagRepo,
                sourceRepo,
                embeddingRepo,
                chatService,
                gigaProps.embeddingsInputsMaxCount()
        );
    }
}
