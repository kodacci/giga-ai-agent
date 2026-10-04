package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class BaseEmbeddingService extends BaseEmbeddingUtilMiddleware implements EmbeddingService {

    private final Transactional trx;
    private final TagRepository tagRepo;
    private final SourceRepository sourceRepo;
    @Getter(value = AccessLevel.PROTECTED)
    private final EmbeddingRepository embeddingRepo;

    protected Either<AppFailure, List<TagData>> saveAllTags(List<TagData> known, List<String> all) {
        val unknown = all.stream()
                .filter(name -> known.stream().noneMatch(tag -> tag.name().equals(name)))
                .toList();

        return tagRepo.create(unknown).map(
                created -> Stream.concat(created.stream(), known.stream()).toList()
        );
    }

    protected abstract Either<AppFailure, List<List<Double>>> createEmbeddings(List<String> chunks);

    protected List<CreateEmbeddingData> toEmbeddingsData(long sourceId, List<List<Double>> vectors, List<String> chunks) {
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
                                        source -> createEmbeddings(data.chunks())
                                                .map(vectors -> toEmbeddingsData(source.id(), vectors, data.chunks()))
                                )
                                .flatMap(embeddingRepo::createEmbeddings)
                )
                .map(List::size);
    }
}
