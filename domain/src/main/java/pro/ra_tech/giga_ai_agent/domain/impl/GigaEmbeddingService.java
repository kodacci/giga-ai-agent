package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.model.EmbeddingResult;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.impl.BaseRestService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingData;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GigaEmbeddingModel;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
public class GigaEmbeddingService extends BaseEmbeddingService implements EmbeddingService {
    private static final int TOO_MANY_TOKENS_HTTP_STATUS = 413;

    private final GigaChatService gigaChatService;
    private final int gigaInputMaxSize;
    private final GigaEmbeddingModel embeddingModel;

    public GigaEmbeddingService(
            Transactional trx,
            TagRepository tagRepo,
            SourceRepository sourceRepo,
            EmbeddingRepository embeddingRepo,
            GigaChatService gigaChatService,
            int gigaInputMaxSize,
            GigaEmbeddingModel embeddingModel
    ) {
        super(trx, tagRepo, sourceRepo, embeddingRepo);
        this.gigaChatService = gigaChatService;
        this.gigaInputMaxSize = gigaInputMaxSize;
        this.embeddingModel = embeddingModel;
    }

    private AppFailure toFailure(Throwable cause) {
        return new DocumentProcessingFailure(
                DocumentProcessingFailure.Code.EMBEDDING_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private Either<AppFailure, CreateEmbeddingsResponse> createEmbeddingsFromChunk(
            List<String> chunks,
            int startIdx,
            int endIdx,
            List<List<Double>> vectors
    ) {
        return Try.of(() -> chunks.subList(startIdx, endIdx))
                .toEither()
                .mapLeft(this::toFailure)
                .flatMap(texts -> gigaChatService.createEmbeddings(texts, embeddingModel))
                .peek(this::logEmbeddingResponse)
                .peek(
                        res -> res.data().forEach(
                                data -> vectors.set(startIdx + data.index(), data.embedding())
                        )
                );
    }

    private int sumUsage(CreateEmbeddingsResponse res) {
        return res.data()
                .stream()
                .map(EmbeddingData::usage)
                .mapToInt(EmbeddingUsage::promptTokens)
                .sum();
    }

    @Override
    protected Either<AppFailure, List<List<Double>>> createEmbeddings(List<String> chunks) {
        val vectors = new ArrayList<List<Double>>(chunks.size());
        IntStream.range(0, chunks.size()).forEach(idx -> vectors.add(null));

        val chunksCount = chunks.size()/ gigaInputMaxSize;
        val tailSize = chunks.size() % gigaInputMaxSize;
        var totalCost = 0;

        for (int i = 0; i < chunksCount; ++i) {
            val idx = i * gigaInputMaxSize;

            val result = createEmbeddingsFromChunk(
                    chunks,
                    idx,
                    idx + gigaInputMaxSize,
                    vectors
            );

            if (result.isLeft()) {
                val ex = result.getLeft().getCause();
                if (!(ex instanceof BaseRestService.RestApiException cause) ||
                        (cause.getHttpCode() != TOO_MANY_TOKENS_HTTP_STATUS)) {
                    return result.peekLeft(failure -> log.error("Error getting embeddings vector"))
                            .map(res -> vectors);
                }

                result.peekLeft(failure -> log.error("Too many tokens failure, skipping chunk...", failure.getCause()));
                continue;
            }

            totalCost += sumUsage(result.get());
            log.info("Processed {}% chunks for embeddings, current total cost: {}", Math.floor(i*100.0/chunksCount), totalCost);
        }

        if (tailSize > 0) {
            val cost = Optional.of(totalCost);

            return createEmbeddingsFromChunk(chunks, chunks.size() - tailSize, chunks.size(), vectors)
                    .peek(res -> log.info("Total cost: {}", cost.get() + sumUsage(res)))
                    .peekLeft(failure -> log.error("Error vectorizing last chunk", failure.getCause()))
                    .fold(
                            failure -> Either.right(vectors),
                            res -> Either.right(vectors)
                    );
        }

        return Either.right(vectors);
    }

    private Either<AppFailure, CreateEmbeddingData> toEmbeddingData(long sourceId, CreateEmbeddingsResponse res, String text) {
        return Try.of(() -> new CreateEmbeddingData(sourceId, toVector(res), text))
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, Void> createEmbedding(String text, long sourceId) {
        return gigaChatService.createEmbeddings(List.of(text), embeddingModel)
                .peek(this::logEmbeddingResponse)
                .flatMap(res -> toEmbeddingData(sourceId, res, text))
                .flatMap(data -> getEmbeddingRepo().createEmbedding(data))
                .peek(data -> log.info("Created Giga Chat embedding in db"))
                .map(data -> null);
    }

    private EmbeddingResult toEmbeddingResult(CreateEmbeddingsResponse res) {
        return new EmbeddingResult(
                toVector(res),
                res.data().stream()
                        .findFirst()
                        .map(EmbeddingData::usage)
                        .map(EmbeddingUsage::promptTokens)
                        .orElse(0)
        );
    }

    @Override
    public Either<AppFailure, EmbeddingResult> getEmbedding(String input) {
        return gigaChatService.createEmbeddings(List.of(input), embeddingModel)
                .map(this::toEmbeddingResult);
    }
}
