package pro.ra_tech.giga_ai_agent.core.services.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.DocumentProcessingTaskStatusResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.DocumentMetadata;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.PdfUploadResponse;
import pro.ra_tech.giga_ai_agent.core.services.api.DocumentService;
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.domain.model.EnqueueDocumentInfo;
import pro.ra_tech.giga_ai_agent.domain.model.InputDocumentMetadata;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private static final DecimalFormat format = new DecimalFormat("###,###,###");

    private final PdfService pdfService;
    private final DocProcessingTaskRepository taskRepo;

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

    @Override
    public Either<AppFailure, EnqueueDocumentInfo> enqueuePdf(MultipartFile file, EnqueueDocumentRequest request) {
        log.info(
                "Enqueueing pdf document {} of size {}, metadata: {}",
                file.getOriginalFilename(),
                format.format(file.getSize()),
                request
        );

        return toBytes(file).flatMap(data -> pdfService.enqueuePdf(
                data, new InputDocumentMetadata(request.documentName(), request.description(), request.tags())
        ));
    }

    @Override
    public Either<AppFailure, DocumentProcessingTaskStatusResponse> getTaskStatus(long taskId) {
        log.info("Getting task {} status", taskId);

        return taskRepo.findById(taskId)
                .map(DocumentProcessingTaskStatusResponse::of);
    }
}
