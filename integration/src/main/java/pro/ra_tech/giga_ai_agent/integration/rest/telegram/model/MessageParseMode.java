package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageParseMode {
    MARKDOWN("Markdown"),
    MARKDOWNV2("MarkdownV2"),
    HTML("HTML");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static MessageParseMode of(String value) {
        return switch (value) {
            case "Markdown" -> MARKDOWN;
            case "MarkdownV2" -> MARKDOWNV2;
            case "HTML" -> HTML;
            default -> throw new IllegalArgumentException("Unknown message parse mode: " + value);
        };
    }
}
