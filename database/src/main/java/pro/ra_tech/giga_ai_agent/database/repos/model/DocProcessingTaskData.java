package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.time.OffsetDateTime;

public record DocProcessingTaskData(
        long id,
        String hfsDocId,
        DocProcessingTaskStatus status,
        int chunksCount,
        int processedChunksCount,
        long sourceId,
        OffsetDateTime createdAt
) {
}
