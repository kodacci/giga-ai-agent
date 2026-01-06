package pro.ra_tech.giga_ai_agent.core.util;

import lombok.SneakyThrows;
import lombok.val;
import org.mockserver.integration.ClientAndServer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class TestUtils {

    @SneakyThrows
    public static byte[] readResourcesFileBytes(String name) {
        try (val stream = TestUtils.class.getClassLoader().getResourceAsStream(name)) {
            if (stream == null) {
                return new byte[] {};
            }

            return stream.readAllBytes();
        }
    }

    @SneakyThrows
    public static String readResourceFileAsString(String name) {
        return new String(readResourcesFileBytes(name), StandardCharsets.UTF_8);
    }

    public static void setupGigaApiAuth(ClientAndServer client) {
        val exp = OffsetDateTime.now().plus(Duration.ofHours(1)).toEpochSecond()*1000;

        client.when(
                request()
                        .withMethod("POST")
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_FORM_URLENCODED)
                        .withPath("/api/v2/oauth")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody("{ \"access_token\": \"TOP_SECRET_TOKEN\", \"expires_at\": " + exp + " }")
        );
    }
}
