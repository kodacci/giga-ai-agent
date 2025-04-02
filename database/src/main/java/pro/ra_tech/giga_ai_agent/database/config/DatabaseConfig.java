package pro.ra_tech.giga_ai_agent.database.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "pro.ra_tech.giga_ai_agent.database.repos.impl"
})
public class DatabaseConfig {
}
