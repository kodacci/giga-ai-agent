package pro.ra_tech.giga_ai_agent.database.repos.model;

import java.util.List;

public record SourceWithTagsDto(
        long id,
        String name,
        String description,
        List<String> tags,
        String hfsDocId
) {
}
