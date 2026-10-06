package pro.ra_tech.giga_ai_agent.domain.impl;

import com.openai.client.OpenAIClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;
import com.openai.services.blocking.EmbeddingService;
import io.vavr.control.Either;
import io.vavr.control.Try;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingPersistentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.config.cloud_ru.CloudRuProps;
import pro.ra_tech.giga_ai_agent.integration.rest.cloud_ru.model.CloudRuEmbeddingModel;

import java.util.List;

import static pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel.CLOUD_RU_BGE_M3;
import static pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel.CLOUD_RU_QWEN3_EMBEDDING_0_6B;
import static pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel.CLOUD_RU_QWEN3_VL_EMBEDDING_2B;
import static pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel.CLOUD_RU_QWEN3_VL_EMBEDDING_8B;

public class QwenEmbeddingsRecalculationService extends BaseEmbeddingsRecalculationService {
    private final EmbeddingService embeddingService;
    private final EmbeddingModel embeddingModel;
    private final pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel dbModel;

    public QwenEmbeddingsRecalculationService(
            EmbeddingsRecalculationTaskRepository taskRepo,
            EmbeddingRepository embeddingRepo,
            KafkaService kafkaService,
            OpenAIClient cloudRuClient,
            CloudRuProps props
    ) {
        super(taskRepo, embeddingRepo, kafkaService);
        this.embeddingService = cloudRuClient.embeddings();
        this.embeddingModel = EmbeddingModel.of(props.embeddingModel().toString());
        this.dbModel = toDbModel(props.embeddingModel());
    }

    private pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel toDbModel(CloudRuEmbeddingModel model) {
        return switch (model) {
            case QWEN3_EMBEDDING_0_6B -> CLOUD_RU_QWEN3_EMBEDDING_0_6B;
            case QWEN3_VL_EMBEDDING_2B -> CLOUD_RU_QWEN3_VL_EMBEDDING_2B;
            case QWEN3_VL_EMBEDDING_8B -> CLOUD_RU_QWEN3_VL_EMBEDDING_8B;
            case BGE_M3 -> CLOUD_RU_BGE_M3;
        };
    }

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.CLOUD_RU_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private Either<AppFailure, List<Double>> getEmbedding(EmbeddingPersistentData data) {
        return Try.of(() -> embeddingService.create(
                EmbeddingCreateParams.builder()
                        .input(data.textData())
                        .model(embeddingModel)
                        .build()
        ))
                .toEither()
                .map(this::toVector)
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, Void> recalculateEmbedding(long embeddingId) {
        return getEmbeddingRepo().findById(embeddingId)
                .flatMap(this::getEmbedding)
                .flatMap(vector -> checkAndWriteVector(vector, embeddingId, dbModel))
                .mapToVoid();
    }
}
