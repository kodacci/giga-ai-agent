package pro.ra_tech.giga_ai_agent.integration.impl;

import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.kafka.core.KafkaTemplate;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaSendResultHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.ChunkProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.util.KafkaSendMonitoringDto;

@Slf4j
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String documentProcessingTopic;
    private final String chunkProcessingTopic;
    private final KafkaSendMonitoringDto docMonitoring;
    private final KafkaSendMonitoringDto chunkMonitoring;

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.KAFKA_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private <T> Either<AppFailure, Void> send(String topic, T task, KafkaSendResultHandler handler, KafkaSendMonitoringDto monitoring) {
        val sample = Timer.start(monitoring.registry());

        return Try.of(
                () -> kafkaTemplate.send(topic, task).whenComplete((result, ex) -> {
                    sample.stop(monitoring.timer());

                    if (ex == null) {
                        handler.handleSuccess();
                    } else {
                        handler.handleError(ex);
                    }
                })
        )
                .toEither()
                .mapLeft(this::toFailure)
                .peekLeft(failure -> sample.stop(monitoring.timer()))
                .map(res -> { sample.stop(monitoring.timer()); return null; });
    }

    @Override
    public Either<AppFailure, Void> enqueueDocumentProcessing(DocumentProcessingTask task, KafkaSendResultHandler handler) {
        log.info("Sending document processing task message to topic {}", documentProcessingTopic);

        return send(documentProcessingTopic, task, handler, docMonitoring)
                .peekLeft(failure -> log.error("Error sending document processing task to kafka:", failure.getCause()))
                .peekLeft(failure -> docMonitoring.sendErrorCounter().increment());
    }

    @Override
    public Either<AppFailure, Void> enqueueChunkProcessing(ChunkProcessingTask task, KafkaSendResultHandler resultHandler) {
        log.info("Sending document chunk {} processing task {} to topic {}", task.chunkIdx(), task.taskId(), chunkProcessingTopic);

        return send(chunkProcessingTopic, task, resultHandler, chunkMonitoring)
                .peekLeft(failure -> log.error("Error sending chunk processing task to kafka: ", failure.getCause()))
                .peekLeft(failure -> chunkMonitoring.sendErrorCounter().increment());
    }

    @Override
    public Either<AppFailure, Void> enqueueEmbeddingRecalculation(EmbeddingRecalculationTask task, KafkaSendResultHandler resultHandler) {
        log.info("Sending embedding for recalculation task {} to topic {}", task.taskId(), "");

        return null;
    }
}
