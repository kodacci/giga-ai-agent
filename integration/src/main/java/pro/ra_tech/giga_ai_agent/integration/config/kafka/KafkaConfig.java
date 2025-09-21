package pro.ra_tech.giga_ai_agent.integration.config.kafka;

import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.impl.KafkaServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.impl.KafkaTaskListener;

import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {
    public static final String DOCUMENT_PROCESSING_TASK_TYPE_MAPPING =
            "documentProcessingTask:pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask";

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory(KafkaProps props) {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                JsonSerializer.TYPE_MAPPINGS, DOCUMENT_PROCESSING_TASK_TYPE_MAPPING
        ));
    }

    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory(KafkaProps props) {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TYPE_MAPPINGS, DOCUMENT_PROCESSING_TASK_TYPE_MAPPING,
                JsonDeserializer.TRUSTED_PACKAGES, "pro.ra_tech.giga_ai_agent.integration.kafka.model"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaContainerFactory(
            ConsumerFactory<String, Object> consumerFactory
    ) {
        val factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);

        val backOff = new FixedBackOff(1000L, 1);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));

        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> kafkaProducerFactory) {
        return new KafkaTemplate<>(kafkaProducerFactory);
    }

    @Bean
    public KafkaService kafkaService(
            KafkaProps props,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new KafkaServiceImpl(
                kafkaTemplate,
                props.documentProcessingTopic(),
                props.chunkProcessingTopic()
        );
    }

    @Bean
    public KafkaTaskListener kafkaTaskListener(KafkaDocProcessingTaskHandler handler) {
        return new KafkaTaskListener(handler);
    }
}
