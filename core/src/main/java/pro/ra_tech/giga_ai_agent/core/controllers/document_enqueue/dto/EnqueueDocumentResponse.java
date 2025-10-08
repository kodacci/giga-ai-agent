package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EnqueueDocumentResponse(
        @NotNull @Positive
        Long taskId,
        @NotNull
        String hfsFileName
) {
}
