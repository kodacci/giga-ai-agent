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
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.TagRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class SourceRepositoryIT implements DatabaseIT {
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
    private SourceRepository repo;
    @Autowired
    private TagRepository tagRepo;
    @Autowired
    private JdbcClient jdbc;

    @AfterEach
    void afterEach() {
        jdbc.sql("DELETE from sources_tags_join").update();
        jdbc.sql("DELETE from tags").update();
        jdbc.sql("DELETE from sources").update();
    }

    @Test
    void shouldListSources() {
        val tag1 = tagRepo.create("tag1").get();
        val tag2 = tagRepo.create("tag2").get();

        val tags = List.of(tag1.id(), tag2.id());
        val result = repo.create(new CreateSourceData("test1", "test1", tags, "hfs1"))
                .flatMap(sources -> repo.create(new CreateSourceData("test2", "test2", tags, "hfs2")))
                .flatMap(sources -> repo.list(0, 10))
                .peekLeft(failure -> log.error("Error creating sources", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val sources = result.get();
        val tagNames = List.of(tag1.name(), tag2.name());
        assertThat(sources.size()).isEqualTo(2);
        assertThat(sources.get(0).name()).isEqualTo("test1");
        assertThat(sources.get(0).description()).isEqualTo("test1");
        assertThat(sources.get(0).tags()).isEqualTo(tagNames);
        assertThat(sources.get(1).name()).isEqualTo("test2");
        assertThat(sources.get(1).description()).isEqualTo("test2");
        assertThat(sources.get(1).tags()).isEqualTo(tagNames);
    }

    @Test
    void shouldListSourcesWithoutTags() {
        val result = repo.create(new CreateSourceData("test1", "test1", List.of(), "hfs1"))
                .flatMap(sources -> repo.list(0, 10));

        assertThat(result.isRight()).isTrue();
        val sources = result.get();
        assertThat(sources.size()).isEqualTo(1);
        assertThat(sources.getFirst().name()).isEqualTo("test1");
        assertThat(sources.getFirst().tags()).isEqualTo(List.of());
    }

    @Test
    void shouldGetNamesByIds() {
        val result = repo.create(new CreateSourceData("test1", "test1", List.of(), null))
                .flatMap(created -> repo.create(new CreateSourceData("test2", "test2", List.of(), null))
                        .map(sources -> List.of(created.id(), sources.id()))
                )
                .flatMap(ids -> repo.getNames(ids));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(List.of("test1", "test2"));
    }
}
