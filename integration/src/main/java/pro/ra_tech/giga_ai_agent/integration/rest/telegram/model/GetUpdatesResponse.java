package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GetUpdatesResponse(
        @JsonProperty("update_id") Integer updateId
) {
}
