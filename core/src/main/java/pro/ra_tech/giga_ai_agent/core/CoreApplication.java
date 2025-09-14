package pro.ra_tech.giga_ai_agent.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pro.ra_tech.giga_ai_agent.integration.config.giga.GigaChatProps;
import pro.ra_tech.giga_ai_agent.integration.config.kafka.KafkaConfig;
import pro.ra_tech.giga_ai_agent.integration.config.kafka.KafkaProps;
import pro.ra_tech.giga_ai_agent.integration.config.telegram.TelegramApiConfig;
import pro.ra_tech.giga_ai_agent.integration.config.telegram.TelegramApiProps;
import pro.ra_tech.giga_ai_agent.integration.config.ya_gpt.YaGptProps;

@SpringBootApplication
@EnableConfigurationProperties({GigaChatProps.class, YaGptProps.class, TelegramApiProps.class, KafkaProps.class})
public class CoreApplication {
    public static void main(String[] args){
        SpringApplication.run(CoreApplication.class, args);
    }
}
