package pro.ra_tech.giga_ai_agent.core.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.ra_tech.giga_ai_agent.core.controllers.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.dto.GetAiModelsResponse;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;

@RestController
@RequestMapping(
        value = "/api/v1/ai-agent",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
public class AiModelController extends BaseController implements AiModelApi {
    private final GigaChatService gigaService;

    @Override
    @GetMapping(value = "/models", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> createGarden() {
        return toResponse(gigaService.listModels().map(GetAiModelsResponse::of));
    }

    @Override
    @PostMapping("/ask")
    public ResponseEntity<Object> askModel(
            @RequestHeader("RqUID") String rqUid,
            @RequestHeader("X-Session-ID") @Nullable String sessionID,
            @RequestBody AskAiModelRequest data
    ) {
        return toResponse(gigaService.askModel(rqUid, data.model(), data.prompt(), sessionID));
    }
}
