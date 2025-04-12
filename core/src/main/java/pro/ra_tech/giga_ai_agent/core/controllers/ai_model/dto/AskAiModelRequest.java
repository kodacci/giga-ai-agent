package pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;

import java.util.List;

public record AskAiModelRequest(
        @JsonProperty("model") @NotEmpty AiModelType model,
        @JsonProperty("prompt") @NotEmpty String prompt,
        @JsonProperty("context") List<String> context,
        @JsonProperty("useEmbeddings") Boolean useEmbeddings
) {
}
