package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public record AiAskMessage(
        @JsonProperty("role") AiRole role,
        @JsonProperty("content") String content,
        @JsonProperty("functions_state_id") @Nullable UUID functionsStateId,
        @JsonProperty("attachments") @Nullable List<String> attachments
) {
}
