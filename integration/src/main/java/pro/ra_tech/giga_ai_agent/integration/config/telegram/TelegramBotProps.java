package pro.ra_tech.giga_ai_agent.integration.config.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.telegram-bot")
public record TelegramBotProps(
        String restApiBaseUrl,
        String apiToken,
        int updateLimit,
        int updateTimeoutSec,
        int requestTimeoutMs,
        int maxRetries,
        int updatesQueueCapacity
) {
}
