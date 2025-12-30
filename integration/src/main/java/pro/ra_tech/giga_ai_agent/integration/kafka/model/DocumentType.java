package pro.ra_tech.giga_ai_agent.integration.kafka.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentType {
    PDF("PDF"),
    TXT("TXT");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static DocumentType of(String value) {
        return switch(value) {
            case "PDF" -> PDF;
            case "TXT" -> TXT;
            default -> throw new IllegalArgumentException(value + " is invalid enum value for " + DocumentType.class.getName());
        };
    }
}
