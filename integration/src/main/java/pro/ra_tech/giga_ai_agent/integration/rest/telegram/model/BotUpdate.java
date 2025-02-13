package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BotUpdate(
        @JsonProperty("update_id") Integer updateId,
        @JsonProperty("message") @Nullable TelegramMessage message,
        @JsonProperty("from") @Nullable TelegramUser user,
        @JsonProperty("date") @Nullable Instant date
) {
    @JsonCreator
    public static Instant mapDate(@JsonProperty("date") long date) {
        return Instant.ofEpochSecond(date);
    }
}
