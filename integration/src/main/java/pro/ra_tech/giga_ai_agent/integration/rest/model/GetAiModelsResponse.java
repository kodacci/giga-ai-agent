package pro.ra_tech.giga_ai_agent.integration.rest.model;

import java.util.List;

public record GetAiModelsResponse(List<AiModelInfo> data, String object) {
}
