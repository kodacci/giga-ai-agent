package pro.ra_tech.giga_ai_agent.integration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaRecalculationTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.ChunkProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;

@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        id = "ai-agent-group",
        topics = {
                "${app.kafka.document-processing-topic}",
                "${app.kafka.chunk-processing-topic}",
                "${app.kafka.embeddings-recalculation-topic}"
        },
        containerFactory = "kafkaContainerFactory"
)
public class KafkaTaskListener {
    private final KafkaDocProcessingTaskHandler docProcessingTaskHandler;
    private final KafkaRecalculationTaskHandler recalculationTaskHandler;

    @KafkaHandler
    public void onDocumentProcessingTask(DocumentProcessingTask task) {
        log.info("Got document processing task: {}", task);

        docProcessingTaskHandler.onDocumentProcessingTask(task);
    }

    @KafkaHandler
    public void onChunkProcessingTask(ChunkProcessingTask task) {
        log.info("Got chunk processing task: {}", task);

        docProcessingTaskHandler.onChunkProcessingTask(task);
    }

    @KafkaHandler
    public void onRecalculationTask(EmbeddingRecalculationTask task) {
        log.info("Got embedding recalculation task: {}", task);

        recalculationTaskHandler.onEmbeddingRecalculation(task);
    }
}
