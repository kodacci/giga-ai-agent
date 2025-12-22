package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaRecalculationTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask;

@Slf4j
public class KafkaRecalculationTaskHandlerImpl implements KafkaRecalculationTaskHandler {
    @Override
    public void onEmbeddingRecalculation(EmbeddingRecalculationTask task) {
        log.info("Recalculating embedding, task: {}", task);
    }
}
