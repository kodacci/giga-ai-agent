package pro.ra_tech.giga_ai_agent.database;

import com.pgvector.PGvector;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateEmbeddingData;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pro.ra_tech.giga_ai_agent.database.Constants.PG_VECTOR_DIMENSIONS;

@Slf4j
class EmbeddingRepositoryIT implements DatabaseIT {
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
    private EmbeddingRepository repo;
    @Autowired
    private SourceRepository sourceRepo;
    @Autowired
    private JdbcClient jdbc;

    private long sourceId = -1;

    @BeforeAll
    void beforeAll() {
        sourceRepo.create(new CreateSourceData("test", "test", List.of(), null))
                .peek(source -> sourceId = source.id())
                .peekLeft(failure -> log.error("Error creating source", failure.getCause()));
    }

    @AfterEach
    void afterEach() {
        jdbc.sql("DELETE FROM embeddings").update();
    }

    private List<Double> generateEmbedding() {
        return new Random().doubles().limit(PG_VECTOR_DIMENSIONS).boxed().toList();
    }

    private Map<String, @Nullable Object> findEmbedding(long id) {
        return jdbc.sql("SELECT vector_data, model FROM embeddings WHERE id = :id")
                .param("id", id)
                .query()
                .singleRow();
    }

    @Test
    @SneakyThrows
    void shouldUpdateVectorData() {
        val update = generateEmbedding();
        val result = repo.createEmbedding(new CreateEmbeddingData(sourceId, generateEmbedding(), "text"))
                .flatMap(data -> repo.updateVector(data.id(), update, EmbeddingModel.EMBEDDINGS_GIGA_R).map(res -> data.id()))
                .peekLeft(failure -> log.error("Error updating vector data", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val id = result.get();
        val embedding = findEmbedding(id);
        assertThat(embedding.get("model")).isEqualTo(EmbeddingModel.EMBEDDINGS_GIGA_R.toString());
        val str = Optional.ofNullable(embedding.get("vector_data")).map(Object::toString).orElse("");
        val vector = new PGvector();
        vector.setValue(str);
        assertThat(vector).isEqualTo(new PGvector(update));
    }

    private List<CreateEmbeddingData> createEmbeddings() {
        val embeddings = List.of(
                new CreateEmbeddingData(sourceId, generateEmbedding(), "text1"),
                new CreateEmbeddingData(sourceId, generateEmbedding(), "text2"),
                new CreateEmbeddingData(sourceId, generateEmbedding(), "text3")
        );

        repo.createEmbeddings(embeddings)
                .peekLeft(failure -> log.error("Error creating embeddings", failure.getCause()));

        return embeddings;
    }

    @Test
    void shouldFindBySourceId() {
        val embeddings = createEmbeddings();
        val result = repo.findBySourceId(sourceId, 0, 10)
                .peekLeft(failure -> log.error("Error searching embeddings", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val found = result.get();
        assertThat(found.size()).isEqualTo(3);
        assertThat(found.getFirst().text()).isEqualTo(embeddings.getFirst().textData());
        assertThat(found.get(1).text()).isEqualTo(embeddings.get(1).textData());
        assertThat(found.get(2).text()).isEqualTo(embeddings.get(2).textData());
    }

    @Test
    void shouldCountBySourceId() {
        val embeddings = createEmbeddings();
        val result = repo.countBySourceId(sourceId)
                .peekLeft(failure -> log.error("Error counting embeddings", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get()).isEqualTo(embeddings.size());
    }

    @Test
    void shouldFindEmbeddingById() {
        val result = repo.createEmbedding(new CreateEmbeddingData(sourceId, generateEmbedding(), "embedding1"))
                .flatMap(data -> repo.findById(data.id()))
                .peekLeft(failure -> log.error("Error finding embedding", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        val found = result.get();
        assertThat(found.textData()).isEqualTo("embedding1");
    }

    @Test
    void shouldFindVectorsByDistance() {
        val vector = generateEmbedding();
        val result = repo.createEmbedding(new CreateEmbeddingData(sourceId, vector, "vector search"))
                .flatMap(data -> repo.vectorSearch(vector))
                .peekLeft(failure -> log.error("Error searching embeddings", failure.getCause()));

        assertThat(result.isRight()).isTrue();
        assertThat(result.get().size()).isEqualTo(1);
    }
}
