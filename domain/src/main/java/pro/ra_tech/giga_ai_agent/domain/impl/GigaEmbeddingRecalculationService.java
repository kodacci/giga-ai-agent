package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.config.giga.GigaChatProps;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GigaEmbeddingModel;

import java.util.List;

@Service
public class GigaEmbeddingRecalculationService extends BaseEmbeddingsRecalculationService {
    private final GigaChatService gigaService;
    private final EmbeddingModel dbModel;
    private final GigaEmbeddingModel embeddingModel;

    public GigaEmbeddingRecalculationService(
            EmbeddingsRecalculationTaskRepository taskRepo,
            EmbeddingRepository embeddingRepo,
            KafkaService kafkaService,
            GigaChatService gigaService,
            GigaChatProps props
    ) {
        super(taskRepo, embeddingRepo, kafkaService);
        this.gigaService = gigaService;
        this.dbModel = toEmbeddingModel(props.embeddingsModel());
        this.embeddingModel = props.embeddingsModel();
    }

    private EmbeddingModel toEmbeddingModel(GigaEmbeddingModel model) {
        return switch (model) {
            case EMBEDDINGS -> EmbeddingModel.GIGA_EMBEDDINGS;
            case EMBEDDINGS_2 -> EmbeddingModel.GIGA_EMBEDDINGS2;
            case EMBEDDINGS_GIGA_R -> EmbeddingModel.GIGA_EMBEDDINGS_GIGA_R;
            case GIGA_EMBEDDINGS_3B_2025_09 -> EmbeddingModel.GIGA_EMBEDDINGS_3B_2025_09;
        };
    }

    @Override
    public Either<AppFailure, Void> recalculateEmbedding(long embeddingId) {
        return getEmbeddingRepo().findById(embeddingId)
                .flatMap(found -> gigaService.createEmbeddings(List.of(found.textData()), embeddingModel))
                .peek(this::logEmbeddingResponse)
                .map(this::toVector)
                .flatMap(vector -> checkAndWriteVector(vector, embeddingId, dbModel))
                .mapToVoid();
    }
}
