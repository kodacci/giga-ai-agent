package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue;

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
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentRequest;
import pro.ra_tech.giga_ai_agent.core.services.api.DocumentService;

@RestController
@RequestMapping(
        value = "/api/v1/documents/enqueue",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
@Slf4j
public class EnqueueDocumentController extends BaseController implements EnqueueDocumentApi {
    private final DocumentService service;

    @Override
    @PostMapping("/pdf")
    public ResponseEntity<Object> enqueuePdf(
            @Valid @RequestPart("file") @NotNull MultipartFile pdf,
            @Valid @RequestPart("metadata") @NotNull EnqueueDocumentRequest request
    ) {
        log.info("Enqueueing document {}", pdf.getOriginalFilename());

        return toResponse(service.enqueuePdf(pdf, request));
    }
}
