package pro.ra_tech.giga_ai_agent.integration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.DocumentProcessingTask;

@RequiredArgsConstructor
@Slf4j
public class KafkaServiceImpl implements KafkaService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String documentProcessingTopic;

    @Override
    public void enqueueDocumentProcessing(DocumentProcessingTask task, KafkaSendResultHandler handler) {
        log.info("Sending document processing task message to topic {}", documentProcessingTopic);

        kafkaTemplate.send(documentProcessingTopic, task).whenComplete((result, ex) -> {
            if (ex == null) {
                handler.handleSuccess();
            } else {
                handler.handleError(ex);
            }
        });
    }
}
