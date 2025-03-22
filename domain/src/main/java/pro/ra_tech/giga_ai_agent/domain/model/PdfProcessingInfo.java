package pro.ra_tech.giga_ai_agent.domain.model;

import java.util.List;

public record PdfProcessingInfo(
        List<String> chunks
) {}
