package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.extern.slf4j.Slf4j;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;

import java.util.List;

@Slf4j
public abstract class BaseEmbeddingService {
    protected void logEmbeddingResponse(CreateEmbeddingsResponse res) {
        log.info(
                "Got {} embeddings with overall cost {}",
                res.data().size(),
                res.data().stream().mapToInt(data -> data.usage().promptTokens()).sum()
        );
    }

    protected List<Double> toVector(CreateEmbeddingsResponse res) {
        return res.data().getFirst().embedding();
    }
}
