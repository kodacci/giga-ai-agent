package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingPersistentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface EmbeddingRepository {
    Either<AppFailure, List<EmbeddingPersistentData>> createEmbeddings(List<CreateEmbeddingData> data);
    Either<AppFailure, List<EmbeddingPersistentData>> vectorSearch(List<Double> promptVector);
}
