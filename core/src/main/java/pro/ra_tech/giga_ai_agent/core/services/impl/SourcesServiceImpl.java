package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.core.controllers.sources.dto.ListSourcesResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.SourcesService;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

@Service
@RequiredArgsConstructor
public class SourcesServiceImpl implements SourcesService {
    private static final int SOURCES_LIMIT = 1000;

    private final SourceRepository repo;

    @Override
    public Either<AppFailure, ListSourcesResponse> listSources() {
        return repo.list(0, SOURCES_LIMIT).map(ListSourcesResponse::new);
    }
}
