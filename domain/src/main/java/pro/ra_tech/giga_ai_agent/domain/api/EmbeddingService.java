package pro.ra_tech.giga_ai_agent.domain.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.domain.model.DocumentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface EmbeddingService {
    Either<AppFailure, Integer> createEmbeddings(DocumentData data);
    Either<AppFailure, Void> createEmbedding(String text, long sourceId);
}
