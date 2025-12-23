package pro.ra_tech.giga_ai_agent.core.controllers.embeddings;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.embeddings.dto.RecalculateRequest;
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
    @PostMapping("/recalculation/enqueue")
    public ResponseEntity<Object> enqueueEmbeddingsForRecalculation(@NotNull @RequestBody RecalculateRequest request) {
        return toResponse(service.enqueueRecalculation(request.sourceId()));
    }

    @Override
    @GetMapping("/recalculation/task/{id}")
    public ResponseEntity<Object> getRecalculationTask(@NotNull @Positive @PathVariable Long id) {
        return toResponse(service.getRecalculationTask(id));
    }
}
