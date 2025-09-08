package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

public record CompletionAlternative(
        @JsonProperty("message") ModelMessage message,
        @JsonProperty("status") Status status
) {

    @RequiredArgsConstructor
    public enum Status {
        ALTERNATIVE_STATUS_UNSPECIFIED("ALTERNATIVE_STATUS_UNSPECIFIED"),
        ALTERNATIVE_STATUS_PARTIAL("ALTERNATIVE_STATUS_PARTIAL"),
        ALTERNATIVE_STATUS_TRUNCATED_FINAL("ALTERNATIVE_STATUS_TRUNCATED_FINAL"),
        ALTERNATIVE_STATUS_FINAL("ALTERNATIVE_STATUS_FINAL"),
        ALTERNATIVE_STATUS_CONTENT_FILTER("ALTERNATIVE_STATUS_CONTENT_FILTER"),
        ALTERNATIVE_STATUS_TOOL_CALLS("ALTERNATIVE_STATUS_TOOL_CALLS");

        private final String value;

        @Override
        @JsonValue
        public String toString() {
            return value;
        }

        @JsonCreator
        public static Status of(@JsonProperty String value) {
            return switch (value) {
                case "ALTERNATIVE_STATUS_PARTIAL" -> ALTERNATIVE_STATUS_PARTIAL;
                case "ALTERNATIVE_STATUS_TRUNCATED_FINAL" -> ALTERNATIVE_STATUS_TRUNCATED_FINAL;
                case "ALTERNATIVE_STATUS_FINAL" -> ALTERNATIVE_STATUS_FINAL;
                case "ALTERNATIVE_STATUS_CONTENT_FILTER" -> ALTERNATIVE_STATUS_CONTENT_FILTER;
                case "ALTERNATIVE_STATUS_TOOL_CALLS" -> ALTERNATIVE_STATUS_TOOL_CALLS;
                default -> ALTERNATIVE_STATUS_UNSPECIFIED;
            };
        }
    }
}
