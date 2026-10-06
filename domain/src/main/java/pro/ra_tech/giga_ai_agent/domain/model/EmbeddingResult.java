package pro.ra_tech.giga_ai_agent.domain.model;

import java.util.List;

public record EmbeddingResult (
    List<Double> vector,
    int tokensUsage
) {
}
