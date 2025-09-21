package pro.ra_tech.giga_ai_agent.integration.util;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public record KafkaSendMonitoringDto(
        MeterRegistry registry,
        Timer timer,
        Counter sendErrorCounter
) {
}
