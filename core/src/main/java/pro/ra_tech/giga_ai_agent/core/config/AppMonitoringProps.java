package pro.ra_tech.giga_ai_agent.core.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.monitoring")
public record AppMonitoringProps (
        @NotEmpty
        String appName,
        @NotEmpty
        String appVersion,
        @NotEmpty
        String podName,
        @NotEmpty
        String podNamespace,
        @NotEmpty
        String nodeName
) {}
