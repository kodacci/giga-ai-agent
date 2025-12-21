package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.time.OffsetDateTime;

public record RecalculationTaskData(
        long id,
        RecalculationTaskStatus status,
        int embeddingsCount,
        int processedEmbeddingsCount,
        long sourceId,
        OffsetDateTime createdAt
) {
}
