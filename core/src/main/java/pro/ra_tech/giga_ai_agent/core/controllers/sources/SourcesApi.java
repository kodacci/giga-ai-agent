package pro.ra_tech.giga_ai_agent.core.controllers.sources;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import pro.ra_tech.giga_ai_agent.core.controllers.sources.dto.ListSourcesResponse;

@Validated
@Tag(name = "Sources")
public interface SourcesApi {
    @Operation(summary = "List known sources")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully got sources list",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ListSourcesResponse.class)
                    )
            )
    })
    ResponseEntity<Object> listSources();
}
