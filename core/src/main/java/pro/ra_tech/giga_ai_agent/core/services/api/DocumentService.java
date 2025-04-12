package pro.ra_tech.giga_ai_agent.core.services.api;

import io.vavr.control.Either;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.DocumentMetadata;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.PdfUploadResponse;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface DocumentService {
    Either<AppFailure, PdfUploadResponse> handlePdf(MultipartFile file, DocumentMetadata metadata);
}
