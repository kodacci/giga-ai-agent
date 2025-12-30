package pro.ra_tech.giga_ai_agent.core.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import pro.ra_tech.giga_ai_agent.integration.config.kafka.KafkaProps;

@TestConfiguration
public class TestConfig {
    @Bean
    public KafkaAdmin.NewTopics topics(KafkaProps kafkaProps) {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(kafkaProps.documentProcessingTopic())
                        .replicas(1)
                        .partitions(1)
                        .build(),
                TopicBuilder.name(kafkaProps.documentProcessingErrorTopic())
                        .replicas(1)
                        .partitions(1)
                        .build(),
                TopicBuilder.name(kafkaProps.chunkProcessingTopic())
                        .replicas(1)
                        .partitions(1)
                        .build()
        );
    }
}
