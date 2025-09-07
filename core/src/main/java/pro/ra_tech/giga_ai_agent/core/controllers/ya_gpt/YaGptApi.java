package pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt;

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
import pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt.dto.AskGptRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt.dto.AskGptResponse;

@Validated
@Tag(name = "AiModel")
public interface YaGptApi {
    @Operation(summary = "Ask Yandex GPT model")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully got AI answer on prompt",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AskGptResponse.class)
                    )
            )
    })
    ResponseEntity<Object> askModel(@RequestBody AskGptRequest request);
}
