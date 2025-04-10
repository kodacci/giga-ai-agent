package pro.ra_tech.giga_ai_agent.integration.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public abstract class BaseIntegrationConfig {
    protected Timer buildTimer(MeterRegistry registry, String service, String method) {
        return Timer.builder("integration.call")
                .tag("integration.service", service)
                .tag("integration.method", method)
                .publishPercentileHistogram()
                .publishPercentiles(0.9, 0.95, 0.99)
                .register(registry);
    }
}
