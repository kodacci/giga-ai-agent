package pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto;

import jakarta.validation.constraints.NotNull;

public record RecalculateResponse(
        @NotNull Long taskId
) {
}