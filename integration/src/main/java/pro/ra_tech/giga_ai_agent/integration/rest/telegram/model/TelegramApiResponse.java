package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramApiResponse<T> (
        @JsonProperty("ok") boolean ok,
        @JsonProperty("description") String description,
        @JsonProperty("result") @Nullable T result,
        @JsonProperty("error") @Nullable String error
) {
}
