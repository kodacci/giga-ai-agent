package pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskStatus;

public record GetRecalculationTaskResponse(
        @Positive @NotNull Long taskId,
        @NotNull RecalculationTaskStatus status,
        @NotNull Double progress
) {
}
