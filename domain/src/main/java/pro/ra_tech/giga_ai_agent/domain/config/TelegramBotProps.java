package pro.ra_tech.giga_ai_agent.domain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;

@Slf4j
@ConfigurationProperties("app.telegram.bot")
public record TelegramBotProps(
        int updatesQueueCapacity,
        AiModelType aiModelType,
        int updatesHandlersCount
) {
}
