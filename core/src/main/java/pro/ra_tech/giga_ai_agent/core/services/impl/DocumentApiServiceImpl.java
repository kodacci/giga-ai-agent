package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.DocumentProcessingTaskStatusResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.DocumentMetadata;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.PdfUploadResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.DocumentApiService;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.domain.api.DocumentService;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.domain.api.TxtService;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentApiServiceImpl implements DocumentApiService {
    private static final DecimalFormat format = new DecimalFormat("###,###,###");

    private final PdfService pdfService;
    private final TxtService txtService;
    private final DocProcessingTaskRepository taskRepo;

    private Either<AppFailure, byte[]> toBytes(MultipartFile file) {
        return Try.of(file::getBytes)
                .toEither()
                .mapLeft(throwable -> new DocumentProcessingFailure(
                        DocumentProcessingFailure.Code.DOCUMENT_PROCESSING_FAILURE,
                        getClass().getName(),
                        throwable
                ));
    }

    @Override
    public Either<AppFailure, PdfUploadResponse> handlePdf(MultipartFile file, DocumentMetadata metadata) {
        log.info(
                "Handling pdf file {} of size {} bytes, metadata: {}",
                file.getOriginalFilename(),
                format.format(file.getSize()),
                metadata
        );

        return toBytes(file).flatMap(data -> pdfService.handlePdf(
                    data, metadata.tags(), metadata.documentName(), metadata.description()
                ))
                .map(PdfUploadResponse::of);
    }

    private InputDocumentMetadata toInputMetadata(EnqueueDocumentRequest request) {
        return new InputDocumentMetadata(request.documentName(), request.description(), request.tags());
    }

    private EnqueueDocumentResponse toResponse(EnqueueDocumentInfo info) {
        return new EnqueueDocumentResponse(info.taskId(), info.hfsFileName());
    }

    private Either<AppFailure, EnqueueDocumentResponse> enqueue(DocumentService service, MultipartFile file, EnqueueDocumentRequest request) {
        log.info(
                "Enqueueing {} document {} of size {}, metadata: {}",
                file.getContentType(),
                file.getOriginalFilename(),
                format.format(file.getSize()),
                request
        );

        return toBytes(file).flatMap(bytes -> service.enqueue(bytes, toInputMetadata(request)))
                .map(this::toResponse);
    }

    @Override
    public Either<AppFailure, EnqueueDocumentResponse> enqueueDocument(MultipartFile file, EnqueueDocumentRequest request) {
        return switch(file.getContentType()) {
            case MediaType.APPLICATION_PDF_VALUE -> enqueue(pdfService, file, request);
            case MediaType.TEXT_PLAIN_VALUE -> enqueue(txtService, file, request);
            case null, default -> Either.left(new DocumentProcessingFailure(
                    DocumentProcessingFailure.Code.UNSUPPORTED_DOCUMENT_CONTENT_TYPE,
                    getClass().getName()
            ));
        };
    }

    @Override
    public Either<AppFailure, DocumentProcessingTaskStatusResponse> getTaskStatus(long taskId) {
        log.info("Getting task {} status", taskId);

        return taskRepo.findById(taskId)
                .map(DocumentProcessingTaskStatusResponse::of);
    }
}
