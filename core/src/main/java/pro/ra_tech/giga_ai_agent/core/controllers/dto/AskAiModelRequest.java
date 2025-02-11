package pro.ra_tech.giga_ai_agent.core.controllers.dto;

import jakarta.validation.constraints.NotEmpty;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelType;

public record AskAiModelRequest(
        @NotEmpty AiModelType model,
        @NotEmpty String prompt
) {
}
