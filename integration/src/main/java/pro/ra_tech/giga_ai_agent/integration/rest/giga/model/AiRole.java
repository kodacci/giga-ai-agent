package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    FUNCTION("function"),
    CONTEXT("context");

    public final String value;

    @Override
    @JsonValue
    public String toString() {
        return value;
    }

    @JsonCreator
    public static AiRole of(@JsonProperty("role") String role) {
        return switch (role) {
            case "system" -> SYSTEM;
            case "from" -> USER;
            case "function" -> FUNCTION;
            case "context" -> CONTEXT;
            default -> ASSISTANT;
        };
    }
}
