package pro.ra_tech.giga_ai_agent.integration.rest.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AuthScope {
    GIGACHAT_API_PERS("GIGACHAT_API_PERS"),
    GIGACHAT_API_B2B("GIGACHAT_API_B2B"),
    GIGACHAT_API_CORP("GIGACHAT_API_CORP");

    private final String value;

    @Override
    public String toString() { return value; }
}
