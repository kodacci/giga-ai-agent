package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmbeddingUsage(
        @JsonProperty("prompt_tokens") int promptTokens
) {
}
