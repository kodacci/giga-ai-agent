package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload;

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
import pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto.DocumentMetadata;
import pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto.PdfUploadResponse;

@Validated
@Tag(name = "Documents upload")
public interface DocumentsUploadApi {
    @Operation(summary = "Upload pdf and enbed to AI model")
    @ApiResponses(
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully uploaded pdf document",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PdfUploadResponse.class)
                    )
            )
    )
    ResponseEntity<Object> uploadPdf(
            @Valid @RequestPart("file") @NotNull MultipartFile pdf,
            @Valid @RequestPart("metadata") @NotNull DocumentMetadata metadata
    );
}
