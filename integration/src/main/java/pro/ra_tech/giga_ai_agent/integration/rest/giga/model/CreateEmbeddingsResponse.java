package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateEmbeddingsResponse(
        @JsonProperty("object") String object,
        @JsonProperty("data") List<EmbeddingData> data,
        @JsonProperty("model") EmbeddingModel model
) {
}
