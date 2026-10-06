package pro.ra_tech.giga_ai_agent.database.repos.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EmbeddingModel {
    // giga chat
    GIGA_EMBEDDINGS("EMBEDDINGS"),
    GIGA_EMBEDDINGS2("EMBEDDINGS2"),
    GIGA_EMBEDDINGS_GIGA_R("EMBEDDINGS_GIGA_R"),
    GIGA_EMBEDDINGS_3B_2025_09("GIGA_EMBEDDINGS_3B_2025_09"),

    // cloud.ru
    CLOUD_RU_QWEN3_EMBEDDING_0_6B("Qwen/Qwen3-Embedding-0.6B"),
    CLOUD_RU_QWEN3_VL_EMBEDDING_2B("Qwen/Qwen3-VL-Embedding-2B"),
    CLOUD_RU_QWEN3_VL_EMBEDDING_8B("Qwen/Qwen3-VL-Embedding-8B"),
    CLOUD_RU_BGE_M3("bge-m3");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
