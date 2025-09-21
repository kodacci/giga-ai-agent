package pro.ra_tech.giga_ai_agent.integration.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.rest.hfs.api.HfsApi;
import pro.ra_tech.giga_ai_agent.integration.rest.hfs.model.CommentRequest;
import pro.ra_tech.giga_ai_agent.integration.util.HttpRequestMonitoringDto;

import java.text.DecimalFormat;

@Slf4j
@RequiredArgsConstructor
public class HfsServiceImpl extends BaseRestService implements HfsService {
    private final DecimalFormat format = new DecimalFormat("###,###,###");

    private final HfsApi api;
    private final String authHeader;

    private final HttpRequestMonitoringDto<Void> uploadMon;
    private final HttpRequestMonitoringDto<ResponseBody> downloadMon;
    private final HttpRequestMonitoringDto<Void> deleteMon;
    private final HttpRequestMonitoringDto<Void> commentMon;

    @Override
    public Either<AppFailure, Void> uploadFile(String folder, String fileName, byte[] fileContent) {
        log.info(
                "Uploading file {} of size {} bytes to folder {}",
                fileName,
                format.format(fileContent.length),
                folder
        );

        val body = RequestBody.create(fileContent, MediaType.parse("application/octet-stream"));

        return sendMeteredRequest(
                uploadMon,
                api.upload(folder, fileName, authHeader, body),
                this::toFailure
        )
                .peekLeft(failure -> log.error("Error uploading file: ", failure.getCause()));
    }

    private Either<AppFailure, byte[]> toByteArray(ResponseBody body) {
        return Try.of(body::bytes)
                .toEither()
                .peekLeft(cause -> log.error("Error converting response body to byte array:", cause))
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, byte[]> downloadFile(String folder, String fileName) {
        log.info("Downloading file {} from folder {}", fileName, folder);

        return sendMeteredRequest(
                downloadMon,
                api.download(folder, fileName, authHeader),
                this::toFailure
        )
                .peekLeft(failure -> log.error("Error downloading file: ", failure.getCause()))
                .flatMap(this::toByteArray);
    }

    @Override
    public Either<AppFailure, Void> comment(String folder, String fileName, String comment) {
        log.info("Commenting file {} in folder {}", fileName, folder);

        return sendMeteredRequest(
                commentMon,
                api.comment(authHeader, new CommentRequest(comment, folder + "/" + fileName)),
                this::toFailure
        )
                .peekLeft(failure -> log.error("Error commenting file: ", failure.getCause()));
    }

    @Override
    public Either<AppFailure, Void> deleteFile(String folder, String fileName) {
        log.info("Deleting file {} from folder {}", fileName, folder);

        return sendMeteredRequest(
                deleteMon,
                api.delete(folder, fileName, authHeader),
                this::toFailure
        )
                .peekLeft(failure -> log.error("Error deleting file: ", failure.getCause()));
    }

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.HFS_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }
}
