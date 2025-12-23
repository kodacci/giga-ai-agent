package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GetUpdatesRequest(
        @JsonProperty("offset") @Nullable Integer offset,
        @JsonProperty("limit") @Nullable Integer limit,
        @JsonProperty("timeout") @Nullable Integer timeout,
        @JsonProperty("allowed_updates") @Nullable List<String> allowedUpdates
) {
}
