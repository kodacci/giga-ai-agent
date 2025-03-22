package pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.model;

import jakarta.validation.constraints.NotNull;

public record SplitTextRequest(
        @NotNull
        String text
) {
}
