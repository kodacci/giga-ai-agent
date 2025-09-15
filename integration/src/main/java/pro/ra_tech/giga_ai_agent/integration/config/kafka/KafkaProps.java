package pro.ra_tech.giga_ai_agent.integration.config.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(
        String bootstrapServers,
        String documentProcessingTopic
) {
}
