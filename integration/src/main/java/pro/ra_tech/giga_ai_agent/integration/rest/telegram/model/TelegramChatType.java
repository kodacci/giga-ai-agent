package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TelegramChatType {
    PRIVATE("private"),
    GROUP("group"),
    SUPERGROUP("supergroup"),
    CHANNEL("channel");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static TelegramChatType of(String value) {
        return switch (value) {
            case "private" -> PRIVATE;
            case "group" -> GROUP;
            case "supergroup" -> SUPERGROUP;
            case "channel" -> CHANNEL;
            default -> throw new IllegalArgumentException("Unknown telegram chat type: " + value);
        };
    }
}
