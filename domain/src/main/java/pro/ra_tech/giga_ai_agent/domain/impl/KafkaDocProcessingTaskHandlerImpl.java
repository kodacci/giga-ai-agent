package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.LlmTextProcessorService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

@Slf4j
@RequiredArgsConstructor
public class KafkaDocProcessingTaskHandlerImpl implements KafkaDocProcessingTaskHandler {
    private final LlmTextProcessorService llmService;

    @Override
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Handling document processing task: {}", task);
    }
}
