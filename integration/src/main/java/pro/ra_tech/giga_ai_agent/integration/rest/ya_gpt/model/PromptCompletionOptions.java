package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromptCompletionOptions(
        @JsonProperty("stream") boolean stream,
        @JsonProperty("temperature") float temperature,
        @JsonProperty("maxTokens") int maxTokens,
        @JsonProperty("reasoningOptions") ReasoningOptions reasoningOptions
) {
}
