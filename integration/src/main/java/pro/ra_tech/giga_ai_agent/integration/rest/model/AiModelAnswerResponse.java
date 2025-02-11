package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiModelAnswerResponse(
        @JsonProperty("choices") List<AiModelChoice> choices,
        @JsonProperty("created_at") Long createdAt,
        @JsonProperty("model") AiModelType model,
        @JsonProperty("usage") AiModelUsage usage,
        @JsonProperty("object") String object
) {
}
