package pro.ra_tech.giga_ai_agent.integration.config.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProps(
        @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
        String documentProcessingTopic
) {
}
