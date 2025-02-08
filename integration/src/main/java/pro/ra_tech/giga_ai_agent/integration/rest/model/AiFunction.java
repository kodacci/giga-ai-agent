package pro.ra_tech.giga_ai_agent.integration.rest.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AiFunction {
    NONE("none"),
    AUTO("auto");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
