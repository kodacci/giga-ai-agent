package pro.ra_tech.giga_ai_agent.integration.rest.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiModelType {
    GIGA_CHAT("GigaChat"),
    GIGA_CHAT_PRO("GigaChat-Pro"),
    GIGA_CHAT_MAX("GigaChat-Max");

    private final String value;

    @Override
    public String toString() { return value; }
}
