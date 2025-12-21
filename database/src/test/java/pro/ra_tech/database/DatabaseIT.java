package pro.ra_tech.database;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.ra_tech.giga_ai_agent.database.config.DatabaseConfig;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = {
                TestApplication.class,
                DatabaseConfig.class
        }
)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public interface DatabaseIT {
}
