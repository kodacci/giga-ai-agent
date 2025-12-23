package pro.ra_tech.giga_ai_agent.database.repos.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RecalculationTaskStatus {
    STARTED("STARTED"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
