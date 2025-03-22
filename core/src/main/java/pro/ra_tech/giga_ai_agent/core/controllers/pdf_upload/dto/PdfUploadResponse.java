package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;

import java.util.List;

public record PdfUploadResponse(
        @NotNull
        List<String> chunks,
        @NotNull
        @Min(0)
        int chunksCount
) {
    public static PdfUploadResponse of(PdfProcessingInfo info) {
        return new PdfUploadResponse(
                info.chunks(),
                info.chunks().size()
        );
    }
}
