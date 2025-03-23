package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DocumentMetadata(
        @NotEmpty
        String documentName,
        @NotNull
        List<String> tags
) {
}
