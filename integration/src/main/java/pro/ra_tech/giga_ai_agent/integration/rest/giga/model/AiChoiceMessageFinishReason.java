package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiChoiceMessageFinishReason {
    STOP("stop"),
    LENGTH("length"),
    FUNCTION_CALL("function_call"),
    BLACKLIST("blacklist"),
    ERROR("error");

    private final String value;

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static AiChoiceMessageFinishReason of(@JsonProperty("finish_reason") String finishReason) {
        return switch (finishReason) {
            case "length" -> LENGTH;
            case "function_call" -> FUNCTION_CALL;
            case "blacklist" -> BLACKLIST;
            case "error" -> ERROR;
            default -> STOP;
        };
    }
}
