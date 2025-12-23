package pro.ra_tech.giga_ai_agent.core;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.ra_tech.giga_ai_agent.core.controllers.sources.dto.ListSourcesResponse;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DisplayName("Sources API test")
@Slf4j
public class SourcesApiIT extends AbstractApiIT {
    private static final String SOURCES_API_URL = "/api/v1/sources";

    @Container
    private static final PostgreSQLContainer pgContainer = new PostgreSQLContainer(
            DockerImageName.parse(Constants.PG_VECTOR_DOCKER_IMAGE_NAME)
    )
            .withDatabaseName("giga_ai_agent")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-db.sql")
            .withExposedPorts(Constants.PG_VECTOR_DOCKER_PORT);

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("apache/kafka-native:3.8.0")
    )
            .withExposedPorts(9092);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    static {
        Startables.deepStart(pgContainer).join();
        Startables.deepStart(kafkaContainer).join();
    }

    @Autowired
    private SourceRepository sourceRepo;

    @Test
    void shouldListSources(@Autowired RestTestClient rest) {
        sourceRepo.create(new CreateSourceData("test1", "test1", List.of(), "hfsId1"))
                .flatMap(data -> sourceRepo.create(new CreateSourceData("test2", "test2", List.of(), "hfsId2")))
                .peekLeft(failure -> log.error("Error creating sources", failure.getCause()))
                .peek(data -> log.info("Created sources"));

        sourceRepo.list(0, 10).peek(res -> log.info("Got sources: {}", res));

        rest.get()
                .uri(SOURCES_API_URL)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ListSourcesResponse.class)
                .consumeWith(result -> {
                    val body = result.getResponseBody();
                    assertThat(body).isNotNull().hasOnlyFields("sources");
                    assertThat(body.sources()).isNotNull().isNotEmpty();
                    assertThat(body.sources().size()).isEqualTo(2);

                    assertThat(body.sources().get(0).name()).isEqualTo("test1");
                    assertThat(body.sources().get(0).description()).isEqualTo("test1");
                    assertThat(body.sources().get(0).tags()).isEqualTo(List.of());
                    assertThat(body.sources().get(0).hfsDocId()).isEqualTo("hfsId1");

                    assertThat(body.sources().get(1).name()).isEqualTo("test2");
                    assertThat(body.sources().get(1).description()).isEqualTo("test2");
                    assertThat(body.sources().get(1).tags()).isEqualTo(List.of());
                    assertThat(body.sources().get(1).hfsDocId()).isEqualTo("hfsId2");
                });
    }
}
