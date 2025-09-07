package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import pro.ra_tech.giga_ai_agent.integration.rest.util.OffsetDateTimeConverter;

import java.time.OffsetDateTime;

public record AuthResponse(
        @JsonProperty("iamToken") String iamToken,
        @JsonProperty("expiresAt") @JsonDeserialize(converter = OffsetDateTimeConverter.class) OffsetDateTime expiresAt
) {
}
