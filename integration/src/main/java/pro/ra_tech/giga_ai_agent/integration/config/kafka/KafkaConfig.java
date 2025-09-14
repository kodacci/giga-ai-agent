package pro.ra_tech.giga_ai_agent.integration.config.kafka;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.impl.KafkaServiceImpl;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaProps.class)
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProps props) {
        return new DefaultKafkaProducerFactory<>(Map.of());
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaService kafkaService(KafkaProps props, KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaServiceImpl(
                kafkaTemplate,
                props.documentProcessingTopic()
        );
    }
}
