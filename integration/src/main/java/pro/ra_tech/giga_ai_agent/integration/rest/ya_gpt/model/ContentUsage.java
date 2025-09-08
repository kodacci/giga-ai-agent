package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ContentUsage (
        @JsonProperty("inputTextTokens") String inputTextTokens,
        @JsonProperty("completionTokens") String completionTokens,
        @JsonProperty("totalTokens") String totalTokens,
        @JsonProperty("completionTokensDetails") CompletionTokensDetails completionTokensDetails
) {
}
