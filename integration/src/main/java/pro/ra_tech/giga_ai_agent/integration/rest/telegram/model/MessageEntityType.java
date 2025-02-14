package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageEntityType {
    MENTION("mention"),
    HASHTAG("hashtag"),
    CASHTAG("cashtag"),
    BOT_COMMAND("bot_command"),
    URL("url"),
    EMAIL("email"),
    PHONE_NUMBER("phone_number"),
    BOLD("bold"),
    ITALIC("italic"),
    UNDERLINE("underline"),
    STRIKETHROUGH("strikethrough"),
    SPOILER("spoiler"),
    BLOCKQUOTE("blockquote"),
    EXPANDABLE_BLOCKQUOTE("expandable_blockquote"),
    CODE("code"),
    PRE("pre"),
    TEXT_LINK("text_link"),
    TEXT_MENTION("text_mention"),
    CUSTOM_EMOJI("custom_emoji");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static MessageEntityType of(String value) {
        return switch (value) {
            case "mention" -> MENTION;
            case "hashtag" -> HASHTAG;
            case "cashtag" -> CASHTAG;
            case "bot_command" -> BOT_COMMAND;
            case "url" -> URL;
            case "email" -> EMAIL;
            case "phone_number" -> PHONE_NUMBER;
            case "bold" -> BOLD;
            case "italic" -> ITALIC;
            case "underline" -> UNDERLINE;
            case "strikethrough" -> STRIKETHROUGH;
            case "spoiler" -> SPOILER;
            case "blockquote" -> BLOCKQUOTE;
            case "expandable_blockquote" -> EXPANDABLE_BLOCKQUOTE;
            case "code" -> CODE;
            case "pre" -> PRE;
            case "text_link" -> TEXT_LINK;
            case "text_mention" -> TEXT_MENTION;
            case "custom_emoji" -> CUSTOM_EMOJI;
            default -> throw new IllegalArgumentException("Unexpected telegram message entity type: " + value);
        };
    }
}
