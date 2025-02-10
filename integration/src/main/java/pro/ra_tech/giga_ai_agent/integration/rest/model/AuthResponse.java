package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(@JsonProperty("access_token") String accessToken, @JsonProperty("expires_at") long expiresAt) {
}
