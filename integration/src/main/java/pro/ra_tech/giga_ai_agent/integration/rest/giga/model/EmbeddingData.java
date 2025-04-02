package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EmbeddingData(
        @JsonProperty("object") String object,
        @JsonProperty("embedding") List<Double> embedding,
        @JsonProperty("index") int index,
        @JsonProperty("usage") EmbeddingUsage usage
) {
}
