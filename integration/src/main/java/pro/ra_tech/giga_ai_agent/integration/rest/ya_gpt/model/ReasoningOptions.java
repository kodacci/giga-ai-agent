package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

public record ReasoningOptions(
        @JsonProperty("mode") Mode mode
) {
    @RequiredArgsConstructor
    public enum Mode {
        DISABLED("DISABLED"),
        ENABLED_HIDDEN("ENABLED_HIDDEN"),
        REASONING_MODE_UNSPECIFIED("REASONING_MODE_UNSPECIFIED");

        private final String value;

        @Override
        @JsonValue
        public String toString() {
            return value;
        }

        @JsonCreator
        public static Mode of(@JsonProperty String value) {
            return switch (value) {
                case "DISABLED" -> DISABLED;
                case "ENABLED_HIDDEN" -> ENABLED_HIDDEN;
                default -> REASONING_MODE_UNSPECIFIED;
            };
        }
    }
}
