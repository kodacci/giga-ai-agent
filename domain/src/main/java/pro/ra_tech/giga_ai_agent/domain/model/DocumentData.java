package pro.ra_tech.giga_ai_agent.domain.model;

import java.util.List;

public record DocumentData(
        String sourceName,
        String sourceDescription,
        List<String> tags,
        List<String> chunks
) {
}
