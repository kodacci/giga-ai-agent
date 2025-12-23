package pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecalculateRequest(
        @Positive
        @NotNull
        Long sourceId
) {
}
