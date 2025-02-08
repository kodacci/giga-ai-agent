package pro.ra_tech.giga_ai_agent.integration.rest.model;

public record AiModelUsage(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {
}
