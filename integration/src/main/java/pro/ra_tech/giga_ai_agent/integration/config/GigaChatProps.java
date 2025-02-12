package pro.ra_tech.giga_ai_agent.integration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.giga-chat")
public record GigaChatProps (
    String authApiBaseUrl,
    String apiBaseUrl,
    int requestTimeoutMs,
    int maxRetries,
    String clientId,
    String authKey,
    int authRetryTimeoutMs
) {
}
