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
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class EmbeddingServiceImpl implements EmbeddingService {
    private final int GIGA_CHAT_EMBEDDING_CHUNKS_MAX_SIZE = 10;

    private final Transactional trx;
    private final TagRepository tagRepo;
    private final SourceRepository sourceRepo;
    private final EmbeddingRepository embeddingRepo;
    private final GigaChatService gigaChatService;

    private Either<AppFailure, List<TagData>> saveAllTags(List<TagData> known, List<String> all) {
        val unknown = all.stream()
                .filter(name -> known.stream().anyMatch(tag -> tag.name().equals(name)))
                .toList();

        return tagRepo.create(unknown).peek(created -> created.addAll(known));
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
                .flatMap(gigaChatService::createEmbeddings)
                .peek(res -> log.info(
                        "Got {} embeddings with overall cost {}",
                        res.data().size(),
                        res.data().stream().mapToInt(data -> data.usage().promptTokens()).sum()
                ))
                .peek(
                        res -> res.data().forEach(
                                data -> vectors.set(startIdx + data.index(), data.embedding())
                        )
                );
    }

    private Either<AppFailure, List<List<Double>>> createGigaEmbeddings(List<String> chunks) {
        val vectors = new ArrayList<List<Double>>(chunks.size());
        val chunksCount = chunks.size()/ GIGA_CHAT_EMBEDDING_CHUNKS_MAX_SIZE;
        val tailSize = chunks.size() % GIGA_CHAT_EMBEDDING_CHUNKS_MAX_SIZE;

        for (int i = 0; i < chunksCount; ++i) {
            val idx = i * GIGA_CHAT_EMBEDDING_CHUNKS_MAX_SIZE;

            val result = createEmbeddingsFromChunk(
                    chunks,
                    idx,
                    idx + GIGA_CHAT_EMBEDDING_CHUNKS_MAX_SIZE,
                    vectors
            );

            if (result.isLeft()) {
                return result.peekLeft(failure -> log.error("Error getting embeddings vector"))
                        .map(res -> vectors);
            }
        }

        if (tailSize > 0) {
            return createEmbeddingsFromChunk(chunks, chunks.size() - tailSize, chunks.size(), vectors)
                    .map(res -> vectors);
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
    public Either<AppFailure, Void> createEmbeddings(DocumentData data) {
        return trx.execute(
                status -> tagRepo.findByNames(data.tags())
                        .flatMap(found -> saveAllTags(found, data.tags()))
                        .map(tags -> tags.stream().map(TagData::id).toList())
                        .flatMap(tags -> sourceRepo.create(new CreateSourceData(data.sourceName(), tags)))
                        .flatMap(
                                source -> createGigaEmbeddings(data.chunks())
                                        .map(vectors -> toEmbeddingsData(source.id(), vectors, data.chunks()))
                        )
                        .flatMap(embeddingRepo::createEmbeddings)
        )
                .map(result -> null);
    }
}
