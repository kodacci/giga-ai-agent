package pro.ra_tech.giga_ai_agent.core.services.impl.cloud_ru;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.cloud_ru.AiModelService;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.model.EmbeddingResult;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

@Service("cloudRuAiModelService")
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {
    private final EmbeddingService cloudRuEmbeddingService;

    private CreateEmbeddingResponse toCreateEmbeddingResponse(EmbeddingResult result) {
        return new CreateEmbeddingResponse(
                result.vector(),
                result.tokensUsage()
        );
    }

    @Override
    public Either<AppFailure, CreateEmbeddingResponse> createEmbedding(CreateEmbeddingRequest request) {
        return cloudRuEmbeddingService.getEmbedding(request.text())
                .map(this::toCreateEmbeddingResponse);
    }
}
