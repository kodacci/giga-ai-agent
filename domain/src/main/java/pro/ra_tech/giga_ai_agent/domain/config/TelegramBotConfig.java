package pro.ra_tech.giga_ai_agent.domain.config;

import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pro.ra_tech.giga_ai_agent.domain.impl.TelegramBotUpdatesHandler;
import pro.ra_tech.giga_ai_agent.domain.impl.TelegramListener;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Configuration
@EnableConfigurationProperties(TelegramBotProps.class)
@ComponentScan("pro.ra_tech.giga_ai_agent.domain.impl")
public class TelegramBotConfig {
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
