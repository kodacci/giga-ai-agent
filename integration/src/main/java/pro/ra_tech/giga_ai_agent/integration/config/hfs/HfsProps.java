package pro.ra_tech.giga_ai_agent.integration.config.hfs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.hfs")
public record HfsProps(
        String apiBaseUrl,
        int requestTimeoutMs,
        int maxRetries,
        int retryTimeoutMs,
        String baseFolder,
        String user,
        String password
) {
}
