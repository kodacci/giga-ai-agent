package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto.DocumentProcessingTaskStatusResponse;
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

    @Operation(summary = "Get document processing task status")
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully got document processing task status",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = DocumentProcessingTaskStatusResponse.class)
                    )
            )
    )
    ResponseEntity<Object> getTaskStatus(
            @PathVariable("taskId") @Positive @NotNull Long taskId
    );

    @Operation(summary = "Enqueue document for processing with AI model")
    @ApiResponse(
            responseCode = "200",
            description = "Successfully enqueued document for processing",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = EnqueueDocumentResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "415",
            description = "Unsupported multipart file media type",
            content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    ResponseEntity<Object> enqueueDocument(
            @Valid @RequestPart("file") @NotNull MultipartFile document,
            @Valid @RequestPart("metadata") @NotNull EnqueueDocumentRequest request
    );
}
