package pro.ra_tech.giga_ai_agent.core.services.impl.cloud_ru;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.cloud_ru.AiModelService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.config.cloud_ru.CloudRuProps;

@Service("cloudRuAiModelService")
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {
    private final OpenAIClient client;
    private final CloudRuProps props;

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.CLOUD_RU_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private CreateEmbeddingResponse toCreateEmbeddingResponse(com.openai.models.embeddings.CreateEmbeddingResponse response) {
        return new CreateEmbeddingResponse(
                response.data()
                        .stream()
                        .map(embedding -> embedding.embedding().stream().map(Float::doubleValue).toList())
                        .toList()
                        .getFirst(),
                (int) response.usage().promptTokens()
        );
    }

    @Override
    public Either<AppFailure, CreateEmbeddingResponse> createEmbedding(CreateEmbeddingRequest request) {
        return Try.of(() -> client.embeddings().create(
                EmbeddingCreateParams.builder()
                        .model(props.embeddingModel().toString())
                        .input(request.text())
                        .build()
        ))
                .toEither()
                .map(this::toCreateEmbeddingResponse)
                .mapLeft(this::toFailure);
    }
}
