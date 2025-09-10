package pro.ra_tech.giga_ai_agent.integration.config;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import pro.ra_tech.giga_ai_agent.integration.util.RequestMonitoringDto;
import retrofit2.Response;

import java.time.Duration;

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

    protected TaskScheduler buildThreadPoolScheduler(int maxThreads, String namePrefix) {
        val scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(maxThreads);
        scheduler.setThreadNamePrefix(namePrefix);
        scheduler.initialize();

        return scheduler;
    }

    protected OkHttpClient buildOkHttpClient(int callTimeoutMs) {
        return new OkHttpClient.Builder()
                .callTimeout(callTimeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();
    }

    protected static <T> RetryPolicy<Response<T>> buildPolicy(int maxRetries, int retryTimeoutMs) {
        return RetryPolicy.<Response<T>>builder().withMaxRetries(maxRetries)
                .withDelay(Duration.ofMillis(retryTimeoutMs))
                .build();
    }

    protected <T> RequestMonitoringDto<T> buildRequestMonitoringDto(
            MeterRegistry registry,
            String service,
            String method,
            int maxRetries,
            int retryTimeoutMs
    ) {
        return new RequestMonitoringDto<>(
                buildTimer(registry, service, method),
                buildCounter(registry, ErrorCounterType.STATUS_4XX, service, method),
                buildCounter(registry, ErrorCounterType.STATUS_5XX, service, method),
                buildPolicy(maxRetries, retryTimeoutMs)
        );
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
