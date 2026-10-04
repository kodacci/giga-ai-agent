package pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v2;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.services.api.cloud_ru.AiModelService;

@RestController
@RequestMapping(
        value = "/api/v2/ai-agent/cloud-ru",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
public class AiModelControllerV2 extends BaseController implements AiModelApi {
    private final AiModelService cloudRuAiModelService;

    @Override
    @PostMapping("/embeddings")
    public ResponseEntity<Object> createEmbedding(
            @RequestHeader("RqUID") String rqUid,
            @RequestBody CreateEmbeddingRequest request
    ) {
        return toResponse(cloudRuAiModelService.createEmbedding(request));
    }
}
