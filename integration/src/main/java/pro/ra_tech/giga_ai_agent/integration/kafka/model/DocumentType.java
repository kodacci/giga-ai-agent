package pro.ra_tech.giga_ai_agent.integration.kafka.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DocumentType {
    PDF("PDF");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static DocumentType of(String value) {
        if (PDF.toString().equals(value)) {
            return PDF;
        }

        throw new IllegalArgumentException(value + " is invalid enum value for " + DocumentType.class.getName());
    }
}
