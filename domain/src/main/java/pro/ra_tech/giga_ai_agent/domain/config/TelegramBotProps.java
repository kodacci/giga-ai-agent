package pro.ra_tech.giga_ai_agent.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.telegram.bot")
public record TelegramBotProps(
        int updatesQueueCapacity
) {
}
