package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmbeddingModel {
    EMBEDDINGS("Embeddings");

    private final String value;

    @Override
    @JsonValue
    public String toString() { return value; }

    @JsonCreator
    public static EmbeddingModel of(@JsonProperty("model") String model) {
        return EMBEDDINGS;
    }
}
