package pro.ra_tech.giga_ai_agent.integration.config.giga;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingModel;

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
    boolean stubEmbeddings,
    EmbeddingModel embeddingModel
) {
}
