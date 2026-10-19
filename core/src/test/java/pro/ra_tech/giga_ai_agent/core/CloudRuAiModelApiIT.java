package pro.ra_tech.giga_ai_agent.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;
import org.mockserver.verify.VerificationTimes;
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
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingRequest;
import pro.ra_tech.giga_ai_agent.core.controllers.ai_model.v1.dto.CreateEmbeddingResponse;
import pro.ra_tech.giga_ai_agent.core.util.Constants;
import pro.ra_tech.giga_ai_agent.core.util.TestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = 10037)
@TestPropertySource(properties = "mockServerPort=10037")
class CloudRuAiModelApiIT extends AbstractApiIT {
    private static final String CLOUD_RU_API_PREFIX = "/api/v2/ai-agent/cloud-ru";
    private static final String EMBEDDING_TEXT =
            "Java - строго типизированный объектно-ориентированный язык программирования";

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

    private static HttpRequest embeddingsRequest() {
        return request()
                .withMethod("POST")
                .withPath("/embeddings")
                .withHeader(Header.header("Authorization", "Bearer test-key"))
                .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/cloud-ru/embeddings-request.json")));
    }

    @AfterEach
    void resetMockServer(ClientAndServer mockServerClient) {
        mockServerClient.reset();
    }

    @Test
    void shouldCreateEmbedding(@Autowired RestTestClient rest, ClientAndServer mockServerClient) {
        mockServerClient.when(embeddingsRequest()).respond(
                response()
                        .withStatusCode(200)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/cloud-ru/embeddings-response.json")))
        );

        rest.post()
                .uri(CLOUD_RU_API_PREFIX + "/embeddings")
                .accept(MediaType.APPLICATION_JSON)
                .header("RqUID", UUID.randomUUID().toString())
                .body(new CreateEmbeddingRequest(EMBEDDING_TEXT))
                .exchange()
                .expectStatus().isOk()
                .expectBody(CreateEmbeddingResponse.class)
                .consumeWith(result -> {
                    val body = result.getResponseBody();
                    assertThat(body).isNotNull().hasOnlyFields("embedding", "usedTokens");
                    assertThat(body.embedding()).isEqualTo(List.of(0.5, -0.25, 0.125, 1.0));
                    assertThat(body.usedTokens()).isEqualTo(12);
                });

        mockServerClient.verify(embeddingsRequest(), VerificationTimes.once());
    }

    @Test
    void shouldReturnProblemWhenCloudRuApiFails(@Autowired RestTestClient rest, ClientAndServer mockServerClient) {
        mockServerClient.when(embeddingsRequest()).respond(
                response()
                        .withStatusCode(400)
                        .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(TestUtils.readResourceFileAsString("mockserver/cloud-ru/embeddings-error-response.json")))
        );

        rest.post()
                .uri(CLOUD_RU_API_PREFIX + "/embeddings")
                .accept(MediaType.APPLICATION_JSON)
                .header("RqUID", UUID.randomUUID().toString())
                .body(new CreateEmbeddingRequest(EMBEDDING_TEXT))
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.code").isEqualTo("CLOUD_RU_INTEGRATION_FAILURE")
                .jsonPath("$.detail").isEqualTo("Integration service API call error");
    }
}
