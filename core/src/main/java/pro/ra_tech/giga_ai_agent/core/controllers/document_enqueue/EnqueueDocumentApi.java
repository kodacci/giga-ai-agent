package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.EnqueueDocumentResponse;

@Validated
@Tag(name = "Documents enqueue")
public interface EnqueueDocumentApi {
    @Operation(summary = "Enqueue PDF document for processing with AI model")
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully enqueued PDF document for processing",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EnqueueDocumentResponse.class)
                    )
            )
    )
    ResponseEntity<Object> enqueuePdf(
            @Valid @RequestPart("file") @NotNull MultipartFile pdf,
            @Valid @RequestPart("metadata") @NotNull EnqueueDocumentRequest request
    );
}
