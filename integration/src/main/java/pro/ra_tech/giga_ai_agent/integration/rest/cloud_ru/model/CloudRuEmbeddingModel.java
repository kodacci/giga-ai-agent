package pro.ra_tech.giga_ai_agent.integration.rest.cloud_ru.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CloudRuEmbeddingModel {
    QWEN3_EMBEDDING_0_6B("Qwen/Qwen3-Embedding-0.6B"),
    QWEN3_VL_EMBEDDING_2B("Qwen/Qwen3-VL-Embedding-2B"),
    QWEN3_VL_EMBEDDING_8B("Qwen/Qwen3-VL-Embedding-8B"),
    BGE_M3("BAAI/bge-m3"),
    GIGA_EMBEDDINGS_INSTRUCT_3B("ai-sage/Giga-Embeddings-instruct-3B"),
    GIGA_EMBEDDINGS_INSTRUCT_480M("ai-sage/Giga-Embeddings-instruct-480M");

    private final String value;

    @Override
    public String toString() { return value; }
}
