package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.util.List;

public record CreateEmbeddingData(
        long sourceId,
        List<Double> vectorData,
        String textData
) {
}
