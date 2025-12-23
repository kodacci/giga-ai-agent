package pro.ra_tech.giga_ai_agent.integration.api;

import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;

public interface KafkaRecalculationTaskHandler {
    void onEmbeddingRecalculation(EmbeddingRecalculationTask task);
}
