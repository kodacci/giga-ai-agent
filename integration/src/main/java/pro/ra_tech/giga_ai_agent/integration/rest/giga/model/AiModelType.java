package pro.ra_tech.giga_ai_agent.integration.rest.giga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiModelType {
    GIGA_CHAT("GigaChat"),
    GIGA_CHAT_PRO("GigaChat-Pro"),
    GIGA_CHAT_PLUS("GigaChat-Plus"),
    GIGA_CHAT_MAX("GigaChat-Max");

    private final String value;

    @Override
    @JsonValue
    public String toString() { return value; }

    @JsonCreator
    public static AiModelType of(@JsonProperty String value) {
        return switch (value) {
            case "GigaChat-Pro" -> GIGA_CHAT_PRO;
            case "GigaChat-Max" -> GIGA_CHAT_MAX;
            case "GigaChat-Plus" -> GIGA_CHAT_PLUS;
            default -> GIGA_CHAT;
        };
    }
}
