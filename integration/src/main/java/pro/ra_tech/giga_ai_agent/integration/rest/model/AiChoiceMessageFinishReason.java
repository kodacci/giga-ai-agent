package pro.ra_tech.giga_ai_agent.integration.rest.model;

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
    public String toString() {
        return value;
    }
}
