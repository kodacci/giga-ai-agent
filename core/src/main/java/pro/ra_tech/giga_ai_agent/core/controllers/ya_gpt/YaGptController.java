package pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt.dto.AskGptRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt.dto.AskGptResponse;
import pro.ra_tech.giga_ai_agent.integration.api.YaGptService;

@RestController
@RequestMapping(
        value = "/api/v1/ya-gpt",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ya-gpt.enabled", havingValue = "true")
public class YaGptController extends BaseController implements YaGptApi {
    private final YaGptService yaGptService;

    @Override
    @PostMapping("/ask")
    public ResponseEntity<Object> askModel(@RequestBody AskGptRequest request) {
        return toResponse(yaGptService.askModel(request.prompt()).map(AskGptResponse::of));
    }
}
