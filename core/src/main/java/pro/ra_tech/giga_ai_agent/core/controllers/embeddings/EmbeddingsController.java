package pro.ra_tech.giga_ai_agent.core.controllers.embeddings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.services.api.EmbeddingsService;

@RestController
@RequestMapping(
        value = "/api/v1/embeddings",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
public class EmbeddingsController extends BaseController implements EmbeddingsApi {
    private final EmbeddingsService service;

    @Override
    @PostMapping("/enqueue")
    public ResponseEntity<Object> enqueueEmbeddingsForRecalculation(Long sourceId) {
        return toResponse(service.enqueueRecalculation(sourceId));
    }
}
