package pro.ra_tech.giga_ai_agent.domain.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.domain.model.PdfProcessingInfo;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface PdfService {
    Either<AppFailure, PdfProcessingInfo> handlePdf(byte[] contents);
}
