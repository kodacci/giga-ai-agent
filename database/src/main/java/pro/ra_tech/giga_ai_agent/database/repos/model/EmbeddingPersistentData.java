package pro.ra_tech.giga_ai_agent.database.repos.model;

public record EmbeddingPersistentData(
        long id,
        long sourceId,
        String textData
) {
}
