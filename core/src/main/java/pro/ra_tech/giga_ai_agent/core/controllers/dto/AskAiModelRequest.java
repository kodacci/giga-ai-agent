package pro.ra_tech.giga_ai_agent.core.controllers.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelType;

public record AskAiModelRequest(
        @NotNull AiModelType model,
        @NotEmpty String prompt
) {
}
