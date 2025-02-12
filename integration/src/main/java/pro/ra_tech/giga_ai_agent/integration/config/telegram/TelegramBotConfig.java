package pro.ra_tech.giga_ai_agent.integration.config.telegram;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TelegramBotProps.class)
public class TelegramBotConfig {
}
