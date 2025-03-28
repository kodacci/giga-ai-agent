package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto.DocumentMetadata;
import pro.ra_tech.giga_ai_agent.core.services.api.DocumentService;

@RestController
@RequestMapping(
        value = "/api/v1/documents/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
public class DocumentUploader extends BaseController implements DocumentsUploadApi {
    private final DocumentService service;

    @Override
    @PostMapping("/pdf")
    public ResponseEntity<Object> uploadPdf(
            @Valid @RequestPart("file") @NotNull MultipartFile pdf,
            @Valid @RequestPart("metadata") @NotNull DocumentMetadata metadata
    ) {
        return toResponse(service.handlePdf(pdf, metadata));
    }
}
