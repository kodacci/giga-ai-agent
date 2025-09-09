package pro.ra_tech.giga_ai_agent.core.controllers.hfs;

import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pro.ra_tech.giga_ai_agent.core.controllers.BaseController;
import pro.ra_tech.giga_ai_agent.domain.api.FileServerService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DocumentProcessingFailure;

@RestController
@RequestMapping("/api/v1/hfs")
@RequiredArgsConstructor
public class HfsController extends BaseController implements HfsApi {
    private final FileServerService service;

    private Either<AppFailure, byte[]> toBytes(MultipartFile file) {
        return Try.of(file::getBytes)
                .toEither()
                .mapLeft(cause -> new DocumentProcessingFailure(
                        DocumentProcessingFailure.Code.HFS_FILE_PROCESSING_FAILURE,
                        getClass().getName(),
                        cause
                ));
    }

    @Override
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_PROBLEM_JSON_VALUE}
    )
    public ResponseEntity<Object> upload(@Valid @NotNull @RequestPart("file") MultipartFile file) {
        return toResponse(toBytes(file).flatMap(bytes -> service.uploadFile(file.getOriginalFilename(), bytes)));
    }
}
