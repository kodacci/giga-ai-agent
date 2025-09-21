package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.ChunkProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;

public interface KafkaService {
    Either<AppFailure, Void> enqueueDocumentProcessing(DocumentProcessingTask task, KafkaSendResultHandler resultHandler);
    Either<AppFailure, Void> enqueueChunkProcessing(ChunkProcessingTask task, KafkaSendResultHandler resultHandler);
}
