package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto.PdfUploadResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.DocumentService;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {
    private final PdfService pdfService;

    private Either<AppFailure, byte[]> toBytes(MultipartFile file) {
        return Try.of(file::getBytes)
                .toEither()
                .mapLeft(throwable -> new DocumentProcessingFailure(
                        DocumentProcessingFailure.Code.PDF_PROCESSING_FAILURE,
                        getClass().getName(),
                        throwable
                ));
    }

    @Override
    public Either<AppFailure, PdfUploadResponse> handlePdf(MultipartFile file) {
        return toBytes(file).flatMap(pdfService::handlePdf).map(PdfUploadResponse::of);
    }
}
