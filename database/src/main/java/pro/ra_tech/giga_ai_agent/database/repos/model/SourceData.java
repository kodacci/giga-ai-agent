package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.util.List;

public record SourceData(
        long id,
        String name,
        List<Long> tags
) {
}
