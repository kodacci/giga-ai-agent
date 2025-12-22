package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto.GetRecalculationTaskResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto.RecalculateResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.EmbeddingsService;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskData;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingsRecalculationService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

@Service
@RequiredArgsConstructor
public class EmbeddingsServiceImpl implements EmbeddingsService {
    private final EmbeddingsRecalculationService recalculationService;
    private final EmbeddingsRecalculationTaskRepository taskRepo;

    @Override
    public Either<AppFailure, RecalculateResponse> enqueueRecalculation(long sourceId) {
        return recalculationService.enqueueAll(sourceId).map(RecalculateResponse::new);
    }

    private GetRecalculationTaskResponse toResponse(RecalculationTaskData task) {
        return new GetRecalculationTaskResponse(
                task.id(),
                task.status(),
                task.embeddingsCount() > 0 ? task.processedEmbeddingsCount()*100/(double)task.embeddingsCount() : 0
        );
    }

    @Override
    public Either<AppFailure, GetRecalculationTaskResponse> getRecalculationTask(long taskId) {
        return taskRepo.findById(taskId).map(this::toResponse);
    }
}
