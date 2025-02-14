package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MessageEntity(
        @JsonProperty("type") MessageEntityType type,
        @JsonProperty("offset") int offset,
        @JsonProperty("length") int length,
        @JsonProperty("language") @Nullable String language
) {
}
