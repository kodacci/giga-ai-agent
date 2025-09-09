package pro.ra_tech.giga_ai_agent.core.controllers.hfs;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.domain.api.FileServerService;

@RestController
@RequestMapping("/api/v1/hfs")
@RequiredArgsConstructor
public class HfsController extends BaseController implements HfsApi {
    private final FileServerService service;

    @Override
    @PostMapping(
            value = "/upload",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
    )
    public ResponseEntity<Object> upload(@RequestBody byte[] fileContent) {
        return toResponse(service.uploadFile(fileContent));
    }
}
