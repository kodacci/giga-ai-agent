package pro.ra_tech.giga_ai_agent.integration.config.giga;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingModel;

@Validated
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
    @NotNull
    EmbeddingModel embeddingsModel
) {
}
