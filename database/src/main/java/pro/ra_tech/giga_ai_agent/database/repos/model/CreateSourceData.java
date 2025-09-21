package pro.ra_tech.giga_ai_agent.database.repos.model;

import org.springframework.lang.Nullable;

import java.util.List;

public record CreateSourceData(
        String name,
        String description,
        List<Long> tags,
        @Nullable
        String hfsDocId
) {
}
