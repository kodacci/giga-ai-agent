package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto;

import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskData;

public record DocumentProcessingTaskStatusResponse (
        DocumentProcessingStatus status,
        int chunksProcessed,
        int chunksCount,
        int progress
){
    private static int toProgress(Integer chunksProcessed, Integer chunksCount) {
        if (chunksProcessed == null || chunksCount == null || chunksProcessed == 0) {
            return 0;
        }

        return (int) Math.floor(100.0 * chunksProcessed / chunksCount);
    }

    public static DocumentProcessingTaskStatusResponse of(DocProcessingTaskData data) {
        return new DocumentProcessingTaskStatusResponse(
                DocumentProcessingStatus.of(data.status()),
                data.processedChunksCount(),
                data.chunksCount(),
                toProgress(data.processedChunksCount(), data.chunksCount())
        );
    }
}
