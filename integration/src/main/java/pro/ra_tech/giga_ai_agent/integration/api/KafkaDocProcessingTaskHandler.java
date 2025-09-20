package pro.ra_tech.giga_ai_agent.integration.api;

import pro.ra_tech.giga_ai_agent.integration.kafka.DocumentProcessingTask;

public interface KafkaDocProcessingTaskHandler {
    void onDocumentProcessingTask(DocumentProcessingTask task);
}
