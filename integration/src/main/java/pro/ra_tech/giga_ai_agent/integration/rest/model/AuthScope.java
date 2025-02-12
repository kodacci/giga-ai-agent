package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthScope {
    GIGA_CHAT_API_PERS("GIGACHAT_API_PERS"),
    GIGA_CHAT_API_B2B("GIGACHAT_API_B2B"),
    GIGA_CHAT_API_CORP("GIGACHAT_API_CORP");

    private final String value;

    @Override
    @JsonValue
    public String toString() { return value; }
}
