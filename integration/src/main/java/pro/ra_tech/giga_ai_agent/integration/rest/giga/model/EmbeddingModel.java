package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum EmbeddingModel {
    EMBEDDINGS("Embeddings");

    private final String value;
    @Getter
    private final String balanceName;

    EmbeddingModel(String value) {
        this.value = value;
        balanceName = value.toLowerCase();
    }

    @Override
    @JsonValue
    public String toString() { return value; }

    @JsonCreator
    public static EmbeddingModel of(@JsonProperty("model") String model) {
        return EMBEDDINGS;
    }
}
