package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface HfsService {
    Either<AppFailure, Void> uploadFile(String folder, String fileName, byte[] fileContent);
    Either<AppFailure, byte[]> downloadFile(String folder, String fileName);
    Either<AppFailure, Void> comment(String folder, String fileName, String comment);
    Either<AppFailure, Void> deleteFile(String folder, String fileName);
}
