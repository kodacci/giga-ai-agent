package pro.ra_tech.giga_ai_agent.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.monitoring")
public record AppMonitoringProps (
        String appName,
        String appVersion,
        String podName,
        String podNamespace,
        String nodeName
) {}
