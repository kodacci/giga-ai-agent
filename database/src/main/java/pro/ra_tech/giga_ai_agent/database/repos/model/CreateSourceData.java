package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.util.List;

public record CreateSourceData(
        String name,
        String description,
        List<Long> tags
) {
}
