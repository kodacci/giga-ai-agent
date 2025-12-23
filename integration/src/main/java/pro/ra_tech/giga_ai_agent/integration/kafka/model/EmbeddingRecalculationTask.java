package pro.ra_tech.giga_ai_agent.integration.kafka.model;

public record EmbeddingRecalculationTask(
        long taskId,
        long sourceId,
        long idx,
        long embeddingId,
        long embeddingsCount
) {
}
