package pro.ra_tech.giga_ai_agent.domain.impl;

import com.openai.client.OpenAIClient;
import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.domain.model.DocumentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public class QwenEmbeddingService extends BaseEmbeddingService {
    private final OpenAIClient client;

    public  QwenEmbeddingService(
            Transactional trx,
            TagRepository tagRepo,
            SourceRepository sourceRepo,
            EmbeddingRepository embeddingRepo,
            final OpenAIClient client
    ) {
        super(trx, tagRepo, sourceRepo, embeddingRepo);
        this.client = client;
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
}
