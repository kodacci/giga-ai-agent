package pro.ra_tech.giga_ai_agent.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pro.ra_tech.giga_ai_agent.integration.config.GigaChatConfig;

@Configuration
@Import(GigaChatConfig.class)
public class MainConfiguration {
}
