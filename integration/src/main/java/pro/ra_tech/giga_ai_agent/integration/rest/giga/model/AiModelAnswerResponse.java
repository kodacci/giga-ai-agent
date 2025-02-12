package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AiModelAnswerResponse(
        @JsonProperty("choices") List<AiModelChoice> choices,
        @JsonProperty("created") Long created,
        @JsonProperty("model") AiModelType model,
        @JsonProperty("usage") AiModelUsage usage,
        @JsonProperty("object") String object
) {
}
