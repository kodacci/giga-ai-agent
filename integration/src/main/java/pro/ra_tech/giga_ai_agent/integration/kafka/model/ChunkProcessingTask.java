package pro.ra_tech.giga_ai_agent.integration.kafka.model;

public record ChunkProcessingTask(
        long taskId,
        long sourceId,
        String hfsDocumentId,
        int chunkIdx,
        String text,
        int chunksCount
) {
}
