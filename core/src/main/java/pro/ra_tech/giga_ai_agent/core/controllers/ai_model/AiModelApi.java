package pro.ra_tech.giga_ai_agent.core.controllers.ai_model;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.GetAiModelsResponse;

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
    ResponseEntity<Object> listAiModels();

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
            @RequestBody AskAiModelRequest data
    );

    @Operation(summary = "Create embeddings vector for specified text")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created embedding vector",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CreateEmbeddingResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<Object> askModel(
            @RequestHeader("RqUID") String rqUid,
            @RequestBody CreateEmbeddingRequest request
    );
}
