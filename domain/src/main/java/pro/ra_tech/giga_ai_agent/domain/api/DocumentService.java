package pro.ra_tech.giga_ai_agent.domain.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface DocumentService {
    Either<AppFailure, EnqueueDocumentInfo> enqueue(
            byte[] contents,
            InputDocumentMetadata meta
    );

    Either<AppFailure, List<String>> splitToChunks(byte[] contents);
}
