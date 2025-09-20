package pro.ra_tech.giga_ai_agent.integration.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

@Slf4j
@RequiredArgsConstructor
@KafkaListener(id = "ai-agent-group", topics = "${app.kafka.document-processing-topic}")
public class KafkaServiceImpl implements KafkaService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String documentProcessingTopic;
    private final KafkaDocProcessingTaskHandler docProcessingTaskHandler;

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.KAFKA_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    public Either<AppFailure, Void> enqueueDocumentProcessing(DocumentProcessingTask task, KafkaSendResultHandler handler) {
        log.info("Sending document processing task message to topic {}", documentProcessingTopic);

        return Try.of(() ->
            kafkaTemplate.send(documentProcessingTopic, task).whenComplete((result, ex) -> {
                if (ex == null) {
                    handler.handleSuccess();
                } else {
                    handler.handleError(ex);
                }
            })
        )
                .toEither()
                .mapLeft(this::toFailure)
                .peekLeft(failure -> log.error("Error sending document processing task to kafka:", failure.getCause()))
                .map(res -> null);
    }

    @KafkaHandler
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Got document processing task: {}", task);

        docProcessingTaskHandler.onDocumentProcessingTask(task);
    }
}
