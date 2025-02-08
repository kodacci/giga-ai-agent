package pro.ra_tech.giga_ai_agent.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import pro.ra_tech.giga_ai_agent.core.controllers.dto.GetAiModelsResponse;

@Validated
@Tag(name = "AiModel")
public interface AiModelApi {
    @Operation(summary = "List available AI models")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully got AI models list",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = GetAiModelsResponse.class)
                    )
            )
        }
    )
    ResponseEntity<Object> createGarden();
}
