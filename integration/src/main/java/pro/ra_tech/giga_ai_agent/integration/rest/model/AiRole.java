package pro.ra_tech.giga_ai_agent.integration.rest.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    FUNCTION("function");

    public final String value;

    @Override
    public String toString() {
        return value;
    }
}
