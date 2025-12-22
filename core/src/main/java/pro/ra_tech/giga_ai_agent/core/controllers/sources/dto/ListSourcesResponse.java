package pro.ra_tech.giga_ai_agent.core.controllers.sources.dto;

import pro.ra_tech.giga_ai_agent.database.repos.model.SourceWithTagsDto;

import java.util.List;

public record ListSourcesResponse(List<SourceWithTagsDto> sources) {
}
