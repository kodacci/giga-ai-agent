package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.domain.api.PdfService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;

@RestController
@RequestMapping(
        value = "/api/v1/pdf/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
public class PdfUploader extends BaseController implements PdfUploadApi {
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
    @PostMapping
    public ResponseEntity<Object> uploadPdf(@RequestParam("file") MultipartFile pdf) {
        return toResponse(toBytes(pdf).flatMap(pdfService::handlePdf));
    }
}
