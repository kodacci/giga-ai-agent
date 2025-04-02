package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface EmbeddingRepository {
    Either<AppFailure, List<EmbeddingData>> createEmbeddings(List<CreateEmbeddingData> data);
}
