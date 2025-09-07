package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AskModelResponse(
        @JsonProperty("alternatives") List<CompletionAlternative> alternatives,
        @JsonProperty("usage") ContentUsage usage,
        @JsonProperty("modelVersion") String modelVersion
) {
}
