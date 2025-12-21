package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingPersistentData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingTextData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface EmbeddingRepository {
    Either<AppFailure, EmbeddingPersistentData> createEmbedding(CreateEmbeddingData data);
    Either<AppFailure, List<EmbeddingPersistentData>> createEmbeddings(List<CreateEmbeddingData> data);
    Either<AppFailure, List<EmbeddingPersistentData>> vectorSearch(List<Double> promptVector);
    Either<AppFailure, Boolean> updateVector(long id, List<Double> vector, EmbeddingModel model);
    Either<AppFailure, List<EmbeddingTextData>> findBySourceId(long sourceId, long offset, int limit);
    Either<AppFailure, Long> countBySourceId(long sourceId);
}
