package pro.ra_tech.giga_ai_agent.domain.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.ai-agent")
public record AiAgentProps(
        @NotEmpty
        String promptBase
) {
}
