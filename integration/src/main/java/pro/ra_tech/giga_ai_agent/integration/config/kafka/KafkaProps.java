package pro.ra_tech.giga_ai_agent.integration.config.kafka;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(
        @NotEmpty
        String bootstrapServers,
        @NotEmpty
        String documentProcessingTopic,
        @NotEmpty
        String documentProcessingErrorTopic,
        @NotEmpty
        String chunkProcessingTopic,
        @NotEmpty
        String embeddingsRecalculationTopic,
        @NotNull
        @Min(1)
        Integer consumerThreadPoolSize
) {
}
