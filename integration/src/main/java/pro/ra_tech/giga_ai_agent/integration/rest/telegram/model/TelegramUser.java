package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramUser(
        @JsonProperty("id") long id,
        @JsonProperty("is_bot") boolean isBot,
        @JsonProperty("first_name") String firstName,
        @JsonProperty("last_name") @Nullable String lastName,
        @JsonProperty("username") @Nullable String userName
) {
}
