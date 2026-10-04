package pro.ra_tech.giga_ai_agent.integration.config.cloud_ru;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.ra_tech.giga_ai_agent.integration.rest.cloud_ru.model.CloudRuEmbeddingModel;

@Validated
@ConfigurationProperties("app.cloud-ru")
public record CloudRuProps(
        String modelApiBaseUrl,
        String modelApiKey,
        CloudRuEmbeddingModel embeddingModel
) {
}
