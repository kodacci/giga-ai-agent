package pro.ra_tech.giga_ai_agent.integration.config.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.backoff.FixedBackOff;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaDocProcessingTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaRecalculationTaskHandler;
import pro.ra_tech.giga_ai_agent.integration.api.KafkaService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.KafkaServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.impl.KafkaTaskListener;

import java.util.Map;

@EnableKafka
@Configuration
@EnableConfigurationProperties(KafkaProps.class)
public class KafkaConfig extends BaseIntegrationConfig {
    public static final String KAFKA_SERVICE = "kafka";
    public static final String DOCUMENT_PROCESSING_TASK_TYPE_MAPPING =
            "documentProcessingTask:pro.ra_tech.giga_ai_agent.integration.kafka.model.DocumentProcessingTask";
    private static final String CHUNK_PROCESSING_TASK_TYPE_MAPPING =
            "chunkProcessingTask:pro.ra_tech.giga_ai_agent.integration.kafka.model.ChunkProcessingTask";
    private static final String EMBEDDINGS_RECALCULATION_TASK_TYPE_MAPPING =
            "embeddingsRecalculationTask:pro.ra_tech.giga_ai_agent.integration.kafka.model.EmbeddingRecalculationTask";

    private static final String TYPE_MAPPINGS = String.join(
            ", ",
            DOCUMENT_PROCESSING_TASK_TYPE_MAPPING,
            CHUNK_PROCESSING_TASK_TYPE_MAPPING,
            EMBEDDINGS_RECALCULATION_TASK_TYPE_MAPPING
    );

    @Bean
    public ProducerFactory<String, Object> kafkaProducerFactory(KafkaProps props) {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class,
                JacksonJsonSerializer.TYPE_MAPPINGS, TYPE_MAPPINGS
        ));
    }

    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory(KafkaProps props) {
        return new DefaultKafkaConsumerFactory<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.bootstrapServers(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class,
                JacksonJsonDeserializer.TYPE_MAPPINGS, TYPE_MAPPINGS,
                JacksonJsonDeserializer.TRUSTED_PACKAGES, "pro.ra_tech.giga_ai_agent.integration.kafka.model"
        ));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            AsyncTaskExecutor kafkaConsumerExecutor
    ) {
        val factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.getContainerProperties().setListenerTaskExecutor(kafkaConsumerExecutor);

        val backOff = new FixedBackOff(1000L, 1);
        factory.setCommonErrorHandler(new DefaultErrorHandler(backOff));

        return factory;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> kafkaProducerFactory) {
        val template = new KafkaTemplate<>(kafkaProducerFactory);
        template.setObservationEnabled(true);

        return template;
    }

    @Bean
    public KafkaService kafkaService(
            KafkaProps props,
            KafkaTemplate<String, Object> kafkaTemplate,
            MeterRegistry registry
    ) {
        return new KafkaServiceImpl(
                kafkaTemplate,
                props.documentProcessingTopic(),
                props.chunkProcessingTopic(),
                props.embeddingsRecalculationTopic(),
                buildKafkaSendMonitoringDto(registry, KAFKA_SERVICE, props.documentProcessingTopic()),
                buildKafkaSendMonitoringDto(registry, KAFKA_SERVICE, props.chunkProcessingTopic()),
                buildKafkaSendMonitoringDto(registry, KAFKA_SERVICE, props.embeddingsRecalculationTopic())
        );
    }

    @Bean
    public KafkaTaskListener kafkaTaskListener(
            KafkaDocProcessingTaskHandler docHandler,
            KafkaRecalculationTaskHandler recalculationHandler
    ) {
        return new KafkaTaskListener(docHandler, recalculationHandler);
    }

    @Bean
    public AsyncTaskExecutor kafkaConsumerExecutor(KafkaProps props) {
        val executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("kafka-consumer-");
        executor.initialize();

        return executor;
    }
}
