package pro.ra_tech.giga_ai_agent.database;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingsRecalculationTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateRecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class EmbeddingsRecalculationTaskRepositoryIT implements DatabaseIT {
    @Container
    private static final PostgreSQLContainer pgContainer = new PostgreSQLContainer(
            DockerImageName.parse("pgvector/pgvector:0.8.1-pg18-trixie")
    )
            .withDatabaseName("giga_ai_agent")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-db.sql")
            .withExposedPorts(5432);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl);
    }

    static {
        Startables.deepStart(pgContainer).join();
    }

    @Autowired
    private EmbeddingsRecalculationTaskRepository repo;
    @Autowired
    private SourceRepository sourceRepo;
    @Autowired
    private JdbcClient jdbc;

    private long sourceId = -1;

    @BeforeAll
    void beforeAll() {
        sourceId = sourceRepo.create(new CreateSourceData("test", "test source", List.of(), null))
                .peekLeft(failure -> log.error("Error creating source", failure.getCause()))
                .get()
                .id();
    }

    @AfterAll
    void afterAll() {
        jdbc.sql("DELETE FROM embeddings_recalculation_tasks").query();
        jdbc.sql("DELETE FROM sources").query();
    }

    @Test
    @DisplayName("Should create and find embedding recalculation task")
    void shouldCreateEmbeddingsRecalculationTask() {
        val count = 10;
        val result = repo.create(new CreateRecalculationTaskData(sourceId, count))
                .flatMap(taskId -> repo.findById(taskId))
                .peekLeft(failure -> log.error("Error creating recalculation task", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val task = result.get();
        assertThat(task).isNotNull();
        assertThat(task.embeddingsCount()).isEqualTo(count);
        assertThat(task.processedEmbeddingsCount()).isEqualTo(0);
        assertThat(task.sourceId()).isEqualTo(sourceId);
    }

    @Test
    void shouldUpdateTaskStatus() {
        val result = repo.create(new CreateRecalculationTaskData(sourceId, 10))
                .flatMap(id -> repo.updateStatus(id, RecalculationTaskStatus.SUCCESS).map(res -> id))
                .flatMap(id -> repo.findById(id))
                .peekLeft(failure -> log.error("Error updating task status", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val task = result.get();
        assertThat(task.status()).isEqualTo(RecalculationTaskStatus.SUCCESS);
    }
}
