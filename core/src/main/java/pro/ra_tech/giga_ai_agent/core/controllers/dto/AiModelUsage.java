package pro.ra_tech.giga_ai_agent.core.controllers.dto;

public record AiModelUsage(
        Integer promptTokens,
        Integer completionTokens,
        Integer precachedPromptTokens,
        Integer totalTokens
) {
    public static AiModelUsage of(pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelUsage usage) {
        return new AiModelUsage(
                usage.promptTokens(),
                usage.completionTokens(),
                usage.precachedPromptTokens(),
                usage.totalTokens()
        );
    }
}
