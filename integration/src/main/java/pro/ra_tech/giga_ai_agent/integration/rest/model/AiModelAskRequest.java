package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.springframework.lang.Nullable;

import java.util.List;

@Builder
public record AiModelAskRequest(
        @JsonProperty("model") AiModelType model,
        @JsonProperty("messages") List<AiAskMessage> messages,
        @JsonProperty("max_tokens") @Nullable Integer maxTokens,
        @JsonProperty("function_call") @Nullable AiFunction functionCall,
        @JsonProperty("functions") @Nullable List<UserFunction> functions,
        @JsonProperty("temperature") @Nullable Float temperature,
        @JsonProperty("top_p") @Nullable Float topP,
        @JsonProperty("replication_penalty") @Nullable Integer repetitionPenalty,
        @JsonProperty("stream") @Nullable Boolean stream,
        @JsonProperty("updated_interval") @Nullable Number updateInterval
) {
}
