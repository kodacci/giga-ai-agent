package pro.ra_tech.giga_ai_agent.integration.util;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import retrofit2.Response;

public record RequestMonitoringDto <T> (
        Timer timer,
        Counter rq4xxCounter,
        Counter rq5xxCounter,
        RetryPolicy<Response<T>> retryPolicy
) {
}
