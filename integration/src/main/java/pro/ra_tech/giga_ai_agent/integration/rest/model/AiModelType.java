package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiModelType {
    GIGA_CHAT("GigaChat"),
    GIGA_CHAT_PRO("GigaChat-Pro"),
    GIGA_CHAT_MAX("GigaChat-Max");

    private final String value;

    @Override
    public String toString() { return value; }

    @JsonCreator
    public static AiModelType of(@JsonProperty String value) {
        return switch (value) {
            case "GigaChat-Pro" -> GIGA_CHAT_PRO;
            case "GigaChat-Max" -> GIGA_CHAT_MAX;
            default -> GIGA_CHAT;
        };
    }
}
