package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto;

import jakarta.validation.constraints.NotNull;

public record EnqueueDocumentResponse(
        @NotNull
        Long taskId
) {
}
