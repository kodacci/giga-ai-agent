package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto;

import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;

public record PdfUploadResponse(
        String text,
        int length,
        int chunks
) {
    public static PdfUploadResponse of(PdfProcessingInfo info) {
        return new PdfUploadResponse(
                info.text(),
                info.textLength(),
                info.chunks()
        );
    }
}
