package pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingResponse;

@Validated
@Tag(name = "AiModel")
@Tag(name = "cloud.ru")
public interface AiModelApi {
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
    ResponseEntity<Object> createEmbedding(
            @RequestHeader("RqUID") String rqUid,
            @RequestBody CreateEmbeddingRequest request
    );
}
