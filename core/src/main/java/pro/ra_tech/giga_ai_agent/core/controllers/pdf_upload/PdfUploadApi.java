package pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.pdf_upload.dto.PdfUploadResponse;

@Validated
@Tag(name = "PDF Upload")
public interface PdfUploadApi {
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
            @RequestParam("file") MultipartFile pdf
    );
}
