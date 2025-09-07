package pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt.dto;

import jakarta.validation.constraints.NotEmpty;

public record AskGptRequest(
        @NotEmpty
        String prompt
) {
}
