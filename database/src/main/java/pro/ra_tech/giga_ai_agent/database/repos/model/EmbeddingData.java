package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.util.List;

public record EmbeddingData(
        long id,
        long source,
        String textData,
        List<Double> vectorData
) {
}
