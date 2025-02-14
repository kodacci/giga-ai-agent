package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public record AiModelUsage(
        @JsonProperty("prompt_tokens") @Nullable Integer promptTokens,
        @JsonProperty("completion_tokens") @Nullable Integer completionTokens,
        @JsonProperty("precached_prompt_tokens") @Nullable Integer precachedPromptTokens,
        @JsonProperty("total_tokens") @Nullable Integer totalTokens
) {
}
