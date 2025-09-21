package pro.ra_tech.giga_ai_agent.integration.kafka.model;

public record ChunkProcessingTask(
        Long taskId,
        Long sourceId,
        String hfsDocumentId,
        Integer chunkIdx,
        String text
) {
}
