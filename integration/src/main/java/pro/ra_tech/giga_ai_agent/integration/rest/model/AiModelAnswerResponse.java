package pro.ra_tech.giga_ai_agent.integration.rest.model;

import java.util.List;

public record AiModelAnswerResponse(
        List<AiModelChoice> choices,
        Long createdAt,
        AiModelType model,
        AiModelUsage usage,
        String object
) {
}
