package pro.ra_tech.giga_ai_agent.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.val;
import pro.ra_tech.giga_ai_agent.domain.api.FileServerService;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;

import java.util.UUID;

@RequiredArgsConstructor
public class FileServerServiceImpl implements FileServerService {
    private final HfsService service;
    private final String folder;

    @Override
    public Either<AppFailure, String> uploadFile(byte[] fileContent) {
        val name = UUID.randomUUID().toString();

        return service.uploadFile(folder, name, fileContent).map(res -> name);
    }
}
