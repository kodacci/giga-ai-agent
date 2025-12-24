package pro.ra_tech.giga_ai_agent.database;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.Transactional;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Slf4j
public class TagRepositoryIT implements DatabaseIT {
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

    @Autowired
    private TagRepository repo;
    @Autowired
    private JdbcClient jdbc;
    @Autowired
    private Transactional trx;

    @AfterEach
    void afterEach() {
        jdbc.sql("DELETE from tags").update();
    }

    @Test
    void shouldCreateTagAndFindByNames() {
        val names = List.of("tag1", "tag2", "tag3");
        val result = repo.create(names)
                .flatMap(tags -> repo.findByNames(names.subList(0, 2)))
                .peekLeft(failure -> log.error("Error creating tags", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val tags = result.get();
        assertThat(tags.size()).isEqualTo(2);
        assertThat(tags.get(0).name()).isEqualTo("tag1");
        assertThat(tags.get(1).name()).isEqualTo("tag2");
    }

    @Test
    void shouldCreateSingleTag() {
        val result = repo.create("tag1")
                .flatMap(data -> repo.findByNames(List.of("tag1")))
                .peekLeft(failure -> log.error("Error creating tag", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val tags = result.get();
        assertThat(tags.size()).isEqualTo(1);
        assertThat(tags.getFirst().name()).isEqualTo("tag1");
    }

    @Test
    void shouldCheckForExistence() {
        val result = repo.create("tag1")
                .flatMap(data -> repo.exists("tag1"))
                .peekLeft(failure -> log.error("Error checking tag existence", failure.getCause()));

        val noResult = repo.exists("tag2");

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isTrue();

        assertThat(noResult.isRight()).isTrue();
        assertThat(noResult.get()).isFalse();
    }
}
