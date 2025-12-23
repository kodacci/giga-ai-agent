package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.Nullable;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.util.TelegramDateDeserializer;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessage(
        @JsonProperty("message_id") int messageId,
        @JsonProperty("chat") TelegramChat chat,
        @JsonProperty("text") @Nullable String text,
        @JsonProperty("from") @Nullable TelegramUser from,
        @JsonProperty("date") @JsonDeserialize(using = TelegramDateDeserializer.class) @Nullable Instant date,
        @JsonProperty("entities") @Nullable List<MessageEntity> entities
) {
}
