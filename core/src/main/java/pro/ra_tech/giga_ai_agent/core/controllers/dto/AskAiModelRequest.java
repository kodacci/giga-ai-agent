package pro.ra_tech.giga_ai_agent.core.controllers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;

public record AskAiModelRequest(
        @JsonProperty("model") @NotNull AiModelType model,
        @JsonProperty("prompt") @NotEmpty String prompt
) {
}
