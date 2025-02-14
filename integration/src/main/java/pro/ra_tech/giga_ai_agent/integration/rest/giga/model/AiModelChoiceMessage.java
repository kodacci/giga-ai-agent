package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public record AiModelChoiceMessage(
        @JsonProperty("role") @Nullable AiRole role,
        @JsonProperty("content") @Nullable String content,
        @JsonProperty("created") @Nullable Long created,
        @JsonProperty("name") @Nullable String name,
        @JsonProperty("functions_state_id") @Nullable String functionsStateId
) {
}
