package pro.ra_tech.giga_ai_agent.integration.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

public abstract class BaseIntegrationConfig {
    protected Timer buildTimer(MeterRegistry registry, String service, String method) {
        return Timer.builder("integration.call")
                .tag("integration.service", service)
                .tag("integration.method", method)
                .publishPercentileHistogram()
                .publishPercentiles(0.9, 0.95, 0.99)
                .register(registry);
    }

    protected Counter buildCounter(MeterRegistry registry, ErrorCounterType type, String service, String method) {
        return Counter.builder("integration." + type.toString())
                .tag("integration.service", service)
                .tag("integration.method", method)
                .register(registry);
    }

    @RequiredArgsConstructor
    public enum ErrorCounterType {
        STATUS_4XX("status.4xx"),
        STATUS_5XX("status.5xx");

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }
}
