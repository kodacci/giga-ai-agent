package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramChat (
        @JsonProperty("id") long id,
        @JsonProperty("type") String type,
        @JsonProperty("title") @Nullable String title
) {
}
