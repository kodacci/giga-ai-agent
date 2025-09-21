package pro.ra_tech.giga_ai_agent.integration.kafka.model;

public record DocumentProcessingTask(
        long taskId,
        long sourceId,
        String hfsDocumentId,
        DocumentType documentType
) {
}
