package pro.ra_tech.giga_ai_agent.database;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
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
import pro.ra_tech.giga_ai_agent.database.repos.api.DocProcessingTaskRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateDocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@Slf4j
@DisplayName("Document processing tasks repository test suit")
public class DocProcessingRepositoryIT implements DatabaseIT {
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

    private long sourceId = -1;

    @Autowired
    private DocProcessingTaskRepository repo;
    @Autowired
    private SourceRepository sourceRepo;
    @Autowired
    private JdbcClient jdbc;

    @BeforeAll
    void beforeAll() {
        sourceRepo.create(new CreateSourceData("test", "test", List.of(), "testHfsId"))
                .peek(source -> sourceId = source.id())
                .peekLeft(failure -> log.error("Error creating source", failure.getCause()));
    }

    @AfterEach
    void afterEach() {
        jdbc.sql("DELETE FROM doc_processing_tasks").update();
    }

    @Test
    void shouldCreateAndFindTask() {
        val res = repo.create(new CreateDocProcessingTaskData(sourceId, "testHfsId"))
                .flatMap(id -> repo.findById(id))
                .peekLeft(failure -> log.error("Error create/find task", failure.getCause()));

        assertThat(res.isRight()).isTrue();
        val task = res.get();
        assertThat(task.sourceId()).isEqualTo(sourceId);
        assertThat(task.hfsDocId()).isEqualTo("testHfsId");
        assertThat(task.status()).isEqualTo(DocProcessingTaskStatus.IDLE);
        assertThat(task.processedChunksCount()).isNull();
        assertThat(task.chunksCount()).isNull();
    }

    @Test
    void shouldUpdateTaskStatus() {

    }

    @Test
    void shouldUpdateTaskProgress() {
        val res = repo.create(new CreateDocProcessingTaskData(sourceId, "testHfsId"))
                .flatMap(id -> repo.updateTaskProgress(id, 1).map(nothing -> id))
                .flatMap(repo::findById)
                .peekLeft(failure -> log.error("Error updating task progress", failure.getCause()));

        assertThat(res.isRight()).isTrue();
        val task = res.get();
        assertThat(task.processedChunksCount()).isEqualTo(1);
    }

    @Test
    void shouldUpdateTaskChunksCount() {
        val res = repo.create(new CreateDocProcessingTaskData(sourceId, "testHfsId"))
                .flatMap(id -> repo.updateTaskChunksCount(id, 1).map(nothing -> id))
                .flatMap(repo::findById)
                .peekLeft(failure -> log.error("Error updating task chunks count", failure.getCause()));

        assertThat(res.isRight()).isTrue();
        val task = res.get();
        assertThat(task.chunksCount()).isEqualTo(1);
    }
}
