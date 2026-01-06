package pro.ra_tech.giga_ai_agent.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.Header;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AiModelUsage;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto.AskAiModelResponse;
import pro.ra_tech.giga_ai_agent.core.util.Constants;
import pro.ra_tech.giga_ai_agent.core.util.TestUtils;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = 10036)
@TestPropertySource(properties = "mockServerPort=10036")
class AiAgentApiIT extends AbstractApiIT {
    private static final String AI_MODEL_API_PREFIX = "/api/v1/ai-agent";

    @Container
    private static final PostgreSQLContainer pgContainer = new PostgreSQLContainer(
            DockerImageName.parse(Constants.PG_VECTOR_DOCKER_IMAGE_NAME)
    )
            .withDatabaseName("giga_ai_agent")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-db.sql")
            .withExposedPorts(Constants.PG_VECTOR_DOCKER_PORT);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl);
    }

    static {
        Startables.deepStart(pgContainer).join();
    }

    private void setupGigaApi(ClientAndServer client, String rqUid) {
        TestUtils.setupGigaApiAuth(client);

        client.when(
                request()
                        .withMethod("POST")
                        .withPath("/api/v1/chat/completions")
                        .withHeaders(
                                Header.header("Content-Type", APPLICATION_JSON_VALUE + "; charset=UTF-8"),
                                Header.header("X-Request-ID", rqUid),
                                Header.header("X-Client-ID", "test")
                        )
                        .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/ai-agent/completions-request.json")))
        ).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/ai-agent/completions-response.json")))
        );
    }

    @Autowired
    private GigaAuthService authService;

    @Test
    void shouldAskModelWithPromptWithoutEmbeddings(@Autowired RestTestClient rest, ClientAndServer mockServerClient) {
        val rqUid = UUID.randomUUID().toString();
        setupGigaApi(mockServerClient, rqUid);

        Awaitility.setDefaultPollInterval(100, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Duration.ofSeconds(30));
        await().until(() -> authService.getAuthHeader().isRight());

        rest.post()
                .uri(AI_MODEL_API_PREFIX + "/ask")
                .accept(MediaType.APPLICATION_JSON)
                .header("RqUID", rqUid)
                .body(new AskAiModelRequest(
                        AiModelType.GIGA_CHAT_2_MAX,
                        "Зачем нужен язык программирования Java?",
                        List.of(),
                        false
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AskAiModelResponse.class)
                .consumeWith(result -> {
                    val body = result.getResponseBody();
                    assertThat(body).isNotNull().hasOnlyFields("messages", "usage");
                    assertThat(body.messages()).isEqualTo(List.of("Чтобы писать программы"));
                    assertThat(body.usage()).isEqualTo(new AiModelUsage(100, 100, 0, 200));
                });
    }
}
