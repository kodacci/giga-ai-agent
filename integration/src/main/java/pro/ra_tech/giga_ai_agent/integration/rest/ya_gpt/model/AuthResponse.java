package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("iamToken") String iamToken
) {
}
