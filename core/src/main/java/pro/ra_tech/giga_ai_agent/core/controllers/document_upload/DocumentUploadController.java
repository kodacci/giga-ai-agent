package pro.ra_tech.giga_ai_agent.core.controllers.document_upload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.document_upload.dto.DocumentMetadata;
import pro.ra_tech.giga_ai_agent.core.services.api.DocumentApiService;

@RestController
@RequestMapping(
        value = "/api/v1/documents/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadController extends BaseController implements DocumentsUploadApi {
    private final DocumentApiService service;

    @Override
    @PostMapping("/pdf")
    public ResponseEntity<Object> uploadPdf(
            @Valid @RequestPart("file") @NotNull MultipartFile pdf,
            @Valid @RequestPart("metadata") @NotNull DocumentMetadata metadata
    ) {
        log.info("Uploading document {}", pdf.getOriginalFilename());

        return toResponse(service.handlePdf(pdf, metadata));
    }
}
