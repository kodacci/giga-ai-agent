package pro.ra_tech.giga_ai_agent.integration.config.ya_gpt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ya-gpt")
public record YaGptProps(
        boolean enabled,
        String authApiBaseUrl,
        String apiBaseUrl,
        String oAuthToken,
        int requestTimeoutMs,
        int maxRetries,
        int retryTimeoutMs,
        String cloudCatalogId
) {
}
