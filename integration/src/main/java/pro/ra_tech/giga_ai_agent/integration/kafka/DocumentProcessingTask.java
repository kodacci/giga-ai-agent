package pro.ra_tech.giga_ai_agent.integration.kafka;

public record DocumentProcessingTask(
        long taskId,
        String hfsDocumentId,
        DocumentType documentType
) {
}
