package pro.ra_tech.giga_ai_agent.core.controllers.embeddings;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto.RecalculateRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto.RecalculateResponse;

@Validated
@Tag(name = "Embeddings")
public interface EmbeddingsApi {
    @Operation(summary = "Enqueue embeddings for recalculation for given source")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully enqueued embeddings for recalculation",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RecalculateResponse.class)
                    )
            )
    })
    ResponseEntity<Object> enqueueEmbeddingsForRecalculation(@NotNull RecalculateRequest request);
}
