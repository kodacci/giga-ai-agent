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
    GIGA_CHAT_MAX("GigaChat-Max"),
    GIGA_CHAT_2("GigaChat-2"),
    GIGA_CHAT_2_PRO("GigaChat-2-Pro"),
    GIGA_CHAT_2_PLUS("GigaChat-2-Plus"),
    GIGA_CHAT_2_MAX("GigaChat-2-Max");

    private final String value;

    @Override
    @JsonValue
    public String toString() { return value; }

    @JsonCreator
    public static AiModelType of(@JsonProperty String value) {
        return switch (value) {
            case "GigaChat" -> GIGA_CHAT;
            case "GigaChat-Pro" -> GIGA_CHAT_PRO;
            case "GigaChat-Max" -> GIGA_CHAT_MAX;
            case "GigaChat-Plus" -> GIGA_CHAT_PLUS;
            case "GigaChat-2-Pro" -> GIGA_CHAT_2_PRO;
            case "GigaChat-2-Max" -> GIGA_CHAT_2_MAX;
            case "GigaChat-2-Plus" -> GIGA_CHAT_2_PLUS;
            default -> GIGA_CHAT_2;
        };
    }
}
