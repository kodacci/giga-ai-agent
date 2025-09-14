package pro.ra_tech.giga_ai_agent.domain.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface PdfService {
    Either<AppFailure, PdfProcessingInfo> handlePdf(
            byte[] contents,
            List<String> tags,
            String name,
            String description
    );

    Either<AppFailure, EnqueueDocumentInfo> enqueuePdf(
            byte[] contents,
            List<String> tags,
            String name,
            String description
    );
}
