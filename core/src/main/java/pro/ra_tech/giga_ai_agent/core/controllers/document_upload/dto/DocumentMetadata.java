package pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DocumentMetadata(
        @NotEmpty String documentName,
        @NotNull List<String> tags,
        @NotEmpty @Size(min = 1, max = 3000) String description
) {
}
