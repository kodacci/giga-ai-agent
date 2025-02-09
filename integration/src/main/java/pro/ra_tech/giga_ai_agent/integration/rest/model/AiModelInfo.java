package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public record AiModelInfo(
        @JsonProperty("id") String id,
        @JsonProperty("object") String object,
        @JsonProperty("owned_by") String ownedBy,
        @JsonProperty("type") @Nullable String type) {
}
