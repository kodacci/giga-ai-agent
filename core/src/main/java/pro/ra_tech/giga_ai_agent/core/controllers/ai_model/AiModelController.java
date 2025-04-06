package pro.ra_tech.giga_ai_agent.core.controllers.ai_model;

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
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.GetAiModelsResponse;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;

import java.util.List;

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
    public ResponseEntity<Object> listAiModels() {
        return toResponse(gigaService.listModels().map(GetAiModelsResponse::of));
    }

    @Override
    @PostMapping("/ask")
    public ResponseEntity<Object> askModel(
            @RequestHeader("RqUID") String rqUid,
            @RequestHeader("X-Session-ID") @Nullable String sessionID,
            @RequestBody AskAiModelRequest data
    ) {
        return toResponse(
                gigaService.askModel(rqUid, data.model(), data.prompt(), sessionID)
                        .map(AskAiModelResponse::of)
        );
    }

    @Override
    @PostMapping("/embeddings")
    public ResponseEntity<Object> askModel(
            @RequestHeader("RqUID") String rqUid,
            @RequestBody CreateEmbeddingRequest request
    ) {
        return toResponse(
                gigaService.createEmbeddings(List.of(request.text()))
                        .map(CreateEmbeddingResponse::of)
        );
    }
}
