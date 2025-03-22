package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import dev.failsafe.retrofit.FailsafeCall;
import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.function.Function;

@Slf4j
public abstract class BaseRestService {
    protected static class ApiException extends RuntimeException {
        public ApiException(String message) {
            super(message);
        }
    }

    protected static <T> RetryPolicy<Response<T>> buildPolicy(int maxRetries) {
        return RetryPolicy.<Response<T>>builder().withMaxRetries(maxRetries).build();
    }

    protected AppFailure toFailure(IntegrationFailure.Code code, String source, @Nullable Throwable cause) {
        return new IntegrationFailure(code, source, cause);
    }

    protected <R> R onResponse(Response<R> response) {
        if (response.isSuccessful()) {
            return response.body();
        }

        try (val body = response.errorBody()) {
            val message = body == null ? "Unknown error" : body.string();
            log.error("API request error with code: {} and body: {}", response.code(), message);
            throw new ApiException(String.format("Bad response with code %d, body: %s", response.code(), message));
        } catch (IOException e) {
            throw new ApiException("Bad response with code " + response.code());
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
            Call<R> call,
            Function<Throwable, AppFailure> toFailure
    ) {
        return Try.of(() -> Failsafe.with(retryPolicy).get(() -> timer.recordCallable(call::execute)))
                .map(this::onResponse)
                .toEither()
                .mapLeft(toFailure);
    }
}
