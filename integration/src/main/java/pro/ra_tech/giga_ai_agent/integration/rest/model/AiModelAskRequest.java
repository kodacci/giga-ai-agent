package pro.ra_tech.giga_ai_agent.integration.rest.model;

import org.springframework.lang.Nullable;

import java.util.List;

public record AiModelAskRequest(
        AiModelType model,
        List<AiAskMessage> messages,
        @Nullable Integer maxTokens,
        @Nullable AiFunction functionCall,
        @Nullable List<UserFunction> functions,
        @Nullable Float temperature,
        @Nullable Float topP,
        @Nullable Integer repetitionPenalty,
        @Nullable Boolean stream,
        @Nullable Number updateInterval
) {
}
