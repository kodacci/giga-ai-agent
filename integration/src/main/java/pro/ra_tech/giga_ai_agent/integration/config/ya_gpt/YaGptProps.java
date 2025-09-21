package pro.ra_tech.giga_ai_agent.integration.config.ya_gpt;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.ya-gpt")
public record YaGptProps(
        boolean enabled,
        @NotEmpty
        String authApiBaseUrl,
        @NotEmpty
        String apiBaseUrl,
        @NotEmpty
        String oAuthToken,
        @Positive
        int requestTimeoutMs,
        @Positive
        int maxRetries,
        @Positive
        int retryTimeoutMs,
        @NotEmpty
        String cloudCatalogId
) {
}
