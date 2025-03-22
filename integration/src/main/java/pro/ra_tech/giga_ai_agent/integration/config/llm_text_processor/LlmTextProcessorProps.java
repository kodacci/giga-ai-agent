package pro.ra_tech.giga_ai_agent.integration.config.llm_text_processor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.llm-text-processor")
public record LlmTextProcessorProps(
        @NotEmpty
        String baseUrl,
        @NotNull
        @Min(1)
        int requestTimeoutMs,
        @NotNull
        @Min(0)
        int maxRetries
) {
}
