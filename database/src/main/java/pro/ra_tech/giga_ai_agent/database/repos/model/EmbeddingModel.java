package pro.ra_tech.giga_ai_agent.database.repos.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmbeddingModel {
    EMBEDDINGS("EMBEDDINGS"),
    EMBEDDINGS2("EMBEDDINGS2"),
    EMBEDDINGS_GIGA_R("EMBEDDINGS_GIGA_R"),
    GIGA_EMBEDDINGS_3B_2025_09("GIGA_EMBEDDINGS_3B_2025_09");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
