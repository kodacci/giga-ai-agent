package pro.ra_tech.giga_ai_agent.core.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import pro.ra_tech.giga_ai_agent.integration.config.giga.GigaChatConfig;
import pro.ra_tech.giga_ai_agent.integration.config.telegram.TelegramBotConfig;

@Configuration
@Slf4j
@Import({GigaChatConfig.class, TelegramBotConfig.class})
@EnableConfigurationProperties(AppMonitoringProps.class)
public class MainConfiguration {
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) { return new CountedAspect(registry); }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> registryCustomizer(AppMonitoringProps props) {
        log.info("Loaded application monitoring props: {}", props);

        return registry -> registry.config().commonTags(
                "app.name", props.appName(),
                "app.version", props.appVersion(),
                "pod.name", props.podName(),
                "pod.namespace", props.podNamespace(),
                "pod.node.name", props.nodeName()
        );
    }
}
