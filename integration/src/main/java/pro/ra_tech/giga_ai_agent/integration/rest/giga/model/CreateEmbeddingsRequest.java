package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateEmbeddingsRequest(
        @JsonProperty("model") EmbeddingModel model,
        @JsonProperty("input") List<String> input
) {
}
