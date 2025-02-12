package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.util.List;

public record GetUpdatesRequest(
        @JsonProperty("offset") @Nullable int offset,
        @JsonProperty("limit") @Nullable int limit,
        @JsonProperty("timeout") @Nullable int timeout,
        @JsonProperty("allowed_updates") @Nullable List<String> allowedUpdates
) {
}
