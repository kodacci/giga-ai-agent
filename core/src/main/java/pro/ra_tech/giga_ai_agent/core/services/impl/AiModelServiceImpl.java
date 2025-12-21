package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.GetAiModelsResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.AiModelService;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingPersistentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.config.giga.GigaChatProps;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingData;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {
    private final GigaChatService gigaService;
    private final EmbeddingRepository embeddingRepo;
    private final GigaChatProps props;

    private Either<AppFailure, AskAiModelResponse> askWithEmbeddings(
            String rqUid,
            AiModelType model,
            String prompt,
            @Nullable String sessionId
    ) {
        return gigaService.createEmbeddings(List.of(prompt), props.embeddingModel())
                .peek(res -> log.info("Created embedding for prompt: {}", res))
                .flatMap(res -> embeddingRepo.vectorSearch(
                        res.data()
                                .stream()
                                .findAny()
                                .map(EmbeddingData::embedding)
                                .orElse(List.of())
                ))
                .peek(embeddings -> log.info("Found embeddings {} in db for prompt {}", embeddings, prompt))
                .flatMap(embeddings -> gigaService.askModel(
                        rqUid,
                        model,
                        prompt,
                        sessionId,
                        embeddings.stream()
                                .map(EmbeddingPersistentData::textData)
                                .toList()
                ))
                .map(AskAiModelResponse::of);
    }

    @Override
    public Either<AppFailure, GetAiModelsResponse> listModels() {
        return gigaService.listModels().map(GetAiModelsResponse::of);
    }

    @Override
    public Either<AppFailure, AskAiModelResponse> askModel(
            String rqUid,
            @Nullable String sessionId,
            AskAiModelRequest data
    ) {
        if (Boolean.TRUE.equals(data.useEmbeddings())) {
            return askWithEmbeddings(rqUid, data.model(), data.prompt(), sessionId);
        }

        return gigaService.askModel(rqUid, data.model(), data.prompt(), sessionId, data.context())
                .map(AskAiModelResponse::of);
    }

    @Override
    public Either<AppFailure, CreateEmbeddingResponse> createEmbedding(CreateEmbeddingRequest request) {
        return gigaService.createEmbeddings(List.of(request.text()), props.embeddingModel())
                .map(CreateEmbeddingResponse::of);
    }
}
