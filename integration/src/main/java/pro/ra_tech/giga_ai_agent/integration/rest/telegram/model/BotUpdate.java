package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.util.TelegramDateDeserializer;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BotUpdate(
        @JsonProperty("update_id") Integer updateId,
        @JsonProperty("message") @Nullable TelegramMessage message,
        @JsonProperty("from") @Nullable TelegramUser user,
        @JsonProperty("date") @JsonDeserialize(using = TelegramDateDeserializer.class) @Nullable Instant date
) {
}
