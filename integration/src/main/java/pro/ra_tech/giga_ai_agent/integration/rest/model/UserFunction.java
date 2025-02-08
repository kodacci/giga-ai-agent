package pro.ra_tech.giga_ai_agent.integration.rest.model;

import org.springframework.lang.Nullable;

import java.util.Map;

public record UserFunction(
        String name,
        @Nullable String description,
        Map<String, Object> parameters
) {
}
