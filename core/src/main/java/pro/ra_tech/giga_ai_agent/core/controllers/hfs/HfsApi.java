package pro.ra_tech.giga_ai_agent.core.controllers.hfs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Validated
@Tag(name = "HFS")
public interface HfsApi {
    @Operation(summary = "Upload file to HFS")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully uploaded file to HFS",
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE)
            )
    })
    ResponseEntity<Object> upload(@RequestPart MultipartFile file);
}
