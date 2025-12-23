package pro.ra_tech.giga_ai_agent.core.controllers.ai_model;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.services.api.AiModelService;

@RestController
@RequestMapping(
        value = "/api/v1/ai-agent",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
public class AiModelController extends BaseController implements AiModelApi {
    private final AiModelService service;

    @Override
    @GetMapping(value = "/models", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<Object> listAiModels() {
        return toResponse(service.listModels());
    }

    @Override
    @PostMapping("/ask")
    public ResponseEntity<Object> askModel(
            @RequestHeader("RqUID") String rqUid,
            @RequestHeader("X-Session-ID") @Nullable String sessionID,
            @RequestBody AskAiModelRequest data
    ) {
        return toResponse(service.askModel(rqUid, sessionID, data));
    }

    @Override
    @PostMapping("/embeddings")
    public ResponseEntity<Object> createEmbedding(
            @RequestHeader("RqUID") String rqUid,
            @RequestBody CreateEmbeddingRequest request
    ) {
        return toResponse(service.createEmbedding(request));
    }
}
