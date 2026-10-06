package pro.ra_tech.giga_ai_agent.domain.impl;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;
import io.vavr.control.Either;
import io.vavr.control.Try;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.domain.model.DocumentData;
import pro.ra_tech.giga_ai_agent.domain.model.EmbeddingResult;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.cloud_ru.model.CloudRuEmbeddingModel;

import java.util.List;

public class QwenEmbeddingService extends BaseEmbeddingService {
    private final OpenAIClient client;
    private final CloudRuEmbeddingModel embeddingModel;

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.CLOUD_RU_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    public  QwenEmbeddingService(
            Transactional trx,
            TagRepository tagRepo,
            SourceRepository sourceRepo,
            EmbeddingRepository embeddingRepo,
            final OpenAIClient client,
            CloudRuEmbeddingModel embeddingModel
    ) {
        super(trx, tagRepo, sourceRepo, embeddingRepo);
        this.client = client;
        this.embeddingModel = embeddingModel;
    }

    @Override
    protected Either<AppFailure, List<List<Double>>> createEmbeddings(List<String> chunks) {
        return Either.right(List.of());
    }

    @Override
    public Either<AppFailure, Integer> createEmbeddings(DocumentData data) {
        return Either.right(0);
    }

    @Override
    public Either<AppFailure, Void> createEmbedding(String text, long sourceId) {
        return Either.right(null);
    }

    private EmbeddingResult toEmbeddingResult(CreateEmbeddingResponse response) {
        return new EmbeddingResult(
                response.data()
                        .stream()
                        .findFirst()
                        .map(embedding -> embedding.embedding().stream().map(Float::doubleValue).toList())
                        .orElse(List.of()),
                (int) response.usage().promptTokens()
        );
    }

    @Override
    public Either<AppFailure, EmbeddingResult> getEmbedding(String input) {
        return Try.of(() -> client.embeddings().create(
                EmbeddingCreateParams.builder()
                        .model(embeddingModel.toString())
                        .input(input)
                        .build()
        ))
                .toEither()
                .map(this::toEmbeddingResult)
                .mapLeft(this::toFailure);
    }
}
