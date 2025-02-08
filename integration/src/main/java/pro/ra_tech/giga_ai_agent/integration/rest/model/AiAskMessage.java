package pro.ra_tech.giga_ai_agent.integration.rest.model;

import org.springframework.lang.Nullable;

import java.util.List;
import java.util.UUID;

public record AiAskMessage(
        AiRole role,
        String content,
        @Nullable UUID functionsStateId,
        List<String> attachments
) {
}
