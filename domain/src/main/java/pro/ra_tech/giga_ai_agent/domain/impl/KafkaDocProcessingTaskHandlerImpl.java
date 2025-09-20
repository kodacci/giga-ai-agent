package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.kafka.DocumentProcessingTask;

@Slf4j
public class KafkaDocProcessingTaskHandlerImpl implements KafkaDocProcessingTaskHandler {
    @Override
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Handling document processing task: {}", task);
    }
}
