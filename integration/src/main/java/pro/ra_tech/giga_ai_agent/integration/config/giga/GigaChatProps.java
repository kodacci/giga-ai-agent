package pro.ra_tech.giga_ai_agent.integration.config.giga;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.giga-chat")
public record GigaChatProps (
    String authApiBaseUrl,
    String apiBaseUrl,
    int requestTimeoutMs,
    int maxRetries,
    int retryTimeoutMs,
    String clientId,
    String authKey,
    int authRetryTimeoutMs,
    @Min(1)
    int embeddingsInputsMaxCount,
    boolean stubEmbeddings
) {
}
