package pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateEmbeddingRequest(
        @NotEmpty
        @Size(min=20, max=1200)
        String text
) {
}
