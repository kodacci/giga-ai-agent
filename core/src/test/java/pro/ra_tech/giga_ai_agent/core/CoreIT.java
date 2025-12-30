package pro.ra_tech.giga_ai_agent.core;

import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import pro.ra_tech.giga_ai_agent.core.util.TestConfig;

@SpringBootTest(
        classes = {
                CoreApplication.class,
                TestConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Testcontainers
public interface CoreIT {
}
