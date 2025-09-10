package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.retrofit.FailsafeCall;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.util.RequestMonitoringDto;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Function;

@Slf4j
public abstract class BaseRestService {
    @Getter
    public static class RestApiException extends RuntimeException {
        private final int httpCode;

        public RestApiException(String message, int httpCode) {
            super(message);
            this.httpCode = httpCode;
        }
    }

    protected static <T> RetryPolicy<Response<T>> buildPolicy(int maxRetries) {
        return RetryPolicy.<Response<T>>builder().withMaxRetries(maxRetries).build();
    }

    protected static <T> RetryPolicy<Response<T>> buildPolicy(int maxRetries, int retryTimeoutMs) {
        return RetryPolicy.<Response<T>>builder().withMaxRetries(maxRetries)
                .withDelay(Duration.ofMillis(retryTimeoutMs))
                .build();
    }

    protected AppFailure toFailure(IntegrationFailure.Code code, String source, @Nullable Throwable cause) {
        return new IntegrationFailure(code, source, cause);
    }

    protected <R> R onResponse(Response<R> response, Counter status4xxCounter, Counter status5xxCounter) {
        if (response.isSuccessful()) {
            return onResponse(response);
        }

        if (response.code() >= 400 && response.code() < 500) {
            status4xxCounter.increment();

            return onResponse(response);
        }

        status5xxCounter.increment();

        return onResponse(response);
    }

    protected <R> R onResponse(Response<R> response) {
        if (response.isSuccessful()) {
            return response.body();
        }

        try (val body = response.errorBody()) {
            val message = body == null ? "Unknown error" : body.string();
            log.error("API request error with code: {} and body: {}", response.code(), message);
            throw new RestApiException(String.format("Bad response with code %d, body: %s", response.code(), message), response.code());
        } catch (IOException e) {
            throw new RestApiException("Bad response with code " + response.code(), response.code());
        }
    }

    protected  <R> Either<AppFailure, R> sendRequest(
            RetryPolicy<Response<R>> retryPolicy,
            Call<R> call,
            Function<Throwable, AppFailure> toFailure
    ) {
        return Try.of(() -> FailsafeCall.with(retryPolicy).compose(call).execute())
                .map(this::onResponse)
                .toEither()
                .mapLeft(toFailure);
    }

    protected <R> Either<AppFailure, R> sendMeteredRequest(
            RetryPolicy<Response<R>> retryPolicy,
            Timer timer,
            Counter status4xxCounter,
            Counter status5xxCounter,
            Call<R> call,
            Function<Throwable, AppFailure> toFailure
    ) {
        return Try.of(
                () -> Failsafe.with(retryPolicy)
                        .get(() -> timer.recordCallable(
                                () -> call.isExecuted() ? call.clone().execute() : call.execute())
                        )
                )
                .map(res -> onResponse(res, status4xxCounter, status5xxCounter))
                .toEither()
                .mapLeft(toFailure);
    }

    protected <R> Either<AppFailure, R> sendMeteredRequest(
            RequestMonitoringDto<R> mon,
            Call<R> call,
            Function<Throwable, AppFailure> toFailure
    ) {
        return sendMeteredRequest(
                mon.retryPolicy(),
                mon.timer(),
                mon.rq4xxCounter(),
                mon.rq5xxCounter(),
                call,
                toFailure
        );
    }
}
