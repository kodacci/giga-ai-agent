package pro.ra_tech.giga_ai_agent.integration.rest.model;

import org.springframework.lang.Nullable;

public record AiModelInfo(String id, String object, String ownedBy, @Nullable String type) {
}
