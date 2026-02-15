package pro.ra_tech.giga_ai_agent.integration.config.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.telegram.api")
public record TelegramApiProps(
        String restApiBaseUrl,
        String apiToken,
        int updateLimit,
        int updateTimeoutSec,
        int requestTimeoutMs,
        int maxRetries
) {
}
