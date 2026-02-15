package pro.ra_tech.giga_ai_agent.domain.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;

@Validated
@ConfigurationProperties("app.telegram.bot")
public record TelegramBotProps(
        boolean enabled,
        int updatesQueueCapacity,
        AiModelType aiModelType,
        int updatesHandlersCount
) {
}
