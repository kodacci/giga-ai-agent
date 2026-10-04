package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum GigaEmbeddingModel {
    EMBEDDINGS("Embeddings"),
    EMBEDDINGS_2("Embeddings-2"),
    EMBEDDINGS_GIGA_R("EmbeddingsGigaR"),
    GIGA_EMBEDDINGS_3B_2025_09("GigaEmbeddings-3B-2025-09");

    private final String value;
    @Getter
    private final String balanceName;

    GigaEmbeddingModel(String value) {
        this.value = value;
        balanceName = value.toLowerCase();
    }

    @Override
    @JsonValue
    public String toString() { return value; }

    @JsonCreator
    public static GigaEmbeddingModel of(@JsonProperty("model") String model) {
        return switch(model) {
            case "Embeddings-2" -> EMBEDDINGS_2;
            case "EmbeddingsGigaR" -> EMBEDDINGS_GIGA_R;
            case "GigaEmbeddings-3B-2025-09" -> GIGA_EMBEDDINGS_3B_2025_09;
            default -> EMBEDDINGS;
        };
    }
}
