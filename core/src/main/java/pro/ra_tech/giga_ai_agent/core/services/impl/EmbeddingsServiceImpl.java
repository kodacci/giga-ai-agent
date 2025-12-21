package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.core.services.api.EmbeddingsService;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingsRecalculationService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

@Service
@RequiredArgsConstructor
public class EmbeddingsServiceImpl implements EmbeddingsService {
    private final EmbeddingsRecalculationService recalculationService;

    @Override
    public Either<AppFailure, Void> enqueueRecalculation(long sourceId) {
        return recalculationService.enqueueAll(sourceId);
    }
}
