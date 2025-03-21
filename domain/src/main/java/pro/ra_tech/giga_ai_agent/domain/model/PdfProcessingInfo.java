package pro.ra_tech.giga_ai_agent.domain.model;

public record PdfProcessingInfo(
        String text,
        int textLength,
        int chunks
) {}
