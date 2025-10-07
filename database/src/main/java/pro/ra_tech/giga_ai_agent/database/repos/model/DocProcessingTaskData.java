package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.time.OffsetDateTime;

public record DocProcessingTaskData(
        long id,
        String hfsDocId,
        DocProcessingTaskStatus status,
        Integer chunksCount,
        Integer processedChunksCount,
        long sourceId,
        OffsetDateTime createdAt
) {
}
