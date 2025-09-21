package pro.ra_tech.giga_ai_agent.integration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        id = "ai-agent-group",
        topics = "${app.kafka.document-processing-topic}",
        containerFactory = "kafkaContainerFactory"
)
public class KafkaTaskListener {
    private final KafkaDocProcessingTaskHandler docProcessingTaskHandler;

    @KafkaHandler
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Got document processing task: {}", task);

        docProcessingTaskHandler.onDocumentProcessingTask(task);
    }
}
