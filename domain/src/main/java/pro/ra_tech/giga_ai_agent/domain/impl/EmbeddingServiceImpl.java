package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.TagData;
import pro.ra_tech.giga_ai_agent.domain.api.EmbeddingService;
import pro.ra_tech.giga_ai_agent.domain.model.DocumentData;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.impl.BaseRestService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingData;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingModel;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class EmbeddingServiceImpl extends BaseEmbeddingService implements EmbeddingService {
    private static final int TOO_MANY_TOKENS_HTTP_STATUS = 413;

    private final Transactional trx;
    private final TagRepository tagRepo;
    private final SourceRepository sourceRepo;
    private final EmbeddingRepository embeddingRepo;
    private final GigaChatService gigaChatService;
    private final int gigaInputMaxSize;
    private final EmbeddingModel embeddingModel;

    private Either<AppFailure, List<TagData>> saveAllTags(List<TagData> known, List<String> all) {
        val unknown = all.stream()
                .filter(name -> known.stream().noneMatch(tag -> tag.name().equals(name)))
                .toList();

        return tagRepo.create(unknown).map(
                created -> Stream.concat(created.stream(), known.stream()).toList()
        );
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

    private Either<AppFailure, List<List<Double>>> createGigaEmbeddings(List<String> chunks) {
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
                    .peekLeft(failure -> log.error("Error vectorising last chunk", failure.getCause()))
                    .fold(
                            failure -> Either.right(vectors),
                            res -> Either.right(vectors)
                    );
        }

        return Either.right(vectors);
    }

    private List<CreateEmbeddingData> toEmbeddingsData(long sourceId, List<List<Double>> vectors, List<String> chunks) {
        return IntStream.range(0, chunks.size())
                .boxed()
                .map(idx -> new CreateEmbeddingData(sourceId, vectors.get(idx), chunks.get(idx)))
                .toList();
    }

    @Override
    public Either<AppFailure, Integer> createEmbeddings(DocumentData data) {
        return trx.execute(
                status -> tagRepo.findByNames(data.tags())
                        .flatMap(found -> saveAllTags(found, data.tags()))
                        .map(tags -> tags.stream().map(TagData::id).toList())
                        .flatMap(tags -> sourceRepo.create(new CreateSourceData(
                                data.sourceName(),
                                data.sourceDescription(),
                                tags,
                                null
                        )))
                        .flatMap(
                                source -> createGigaEmbeddings(data.chunks())
                                        .map(vectors -> toEmbeddingsData(source.id(), vectors, data.chunks()))
                        )
                        .flatMap(embeddingRepo::createEmbeddings)
        )
                .map(List::size);
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
                .flatMap(embeddingRepo::createEmbedding)
                .peek(data -> log.info("Created embedding in db"))
                .map(data -> null);
    }
}
