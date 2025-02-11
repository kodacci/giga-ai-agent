package pro.ra_tech.giga_ai_agent.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestHeader;
import pro.ra_tech.giga_ai_agent.core.controllers.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.dto.GetAiModelsResponse;
import retrofit2.http.Body;

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

    @Operation(summary = "Ask AI model with prompt")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully got AI answer on prompt",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AskAiModelResponse.class)
                    )
            )
    })
    ResponseEntity<Object> askModel(
            @RequestHeader("RqUID") String rqUid,
            @RequestHeader("X-Session-ID") @Nullable String sessionID,
            @Body AskAiModelRequest data
    );
}
