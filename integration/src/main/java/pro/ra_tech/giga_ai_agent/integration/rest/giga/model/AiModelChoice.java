package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public record AiModelChoice(
        @JsonProperty("message") @Nullable AiModelChoiceMessage message,
        @JsonProperty("index") @Nullable Integer index,
        @JsonProperty("finish_reason") @Nullable AiChoiceMessageFinishReason finishReason
) {
}
