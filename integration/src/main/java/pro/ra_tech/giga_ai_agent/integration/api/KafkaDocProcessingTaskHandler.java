package pro.ra_tech.giga_ai_agent.integration.api;

import pro.ra_tech.giga_ai_agent.integration.kafka.model.ChunkProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

public interface KafkaDocProcessingTaskHandler {
    void onDocumentProcessingTask(DocumentProcessingTask task);
    void onChunkProcessingTask(ChunkProcessingTask task);
}
