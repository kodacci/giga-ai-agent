package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AskModelRequest(
        @JsonProperty("modelUri") String modelUri,
        @JsonProperty("completionOptions") PromptCompletionOptions completionOptions,
        @JsonProperty("messages") List<ModelMessage> messages
) {
}
