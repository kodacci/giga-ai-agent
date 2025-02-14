package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import dev.failsafe.retrofit.FailsafeCall;
import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AuthResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AuthScope;
import retrofit2.Response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
@Slf4j
public class GigaAuthServiceImpl extends BaseRestService implements GigaAuthService {
    private final static int EXPIRES_TIMEOUT_CORRECTION_SEC = 5;

    @RequiredArgsConstructor
    private class AuthUpdater implements Runnable {
        private final ReentrantLock mutex;

        @Synchronized("mutex")
        public void run() {
            authenticate().peek(res -> {
                authHeader = "Bearer " + res.accessToken();
                val exp = Instant.ofEpochMilli(res.expiresAt());
                taskScheduler.schedule(
                        new AuthUpdater(mutex),
                        exp.minus(EXPIRES_TIMEOUT_CORRECTION_SEC, ChronoUnit.SECONDS)
                );
            })
                    .peekLeft(failure -> {
                        authHeader = null;
                        taskScheduler.schedule(
                                    new AuthUpdater(mutex),
                                    Instant.now().plus(authRetryTimeoutMs, ChronoUnit.MILLIS)
                                );
                    });
        }
    }

    private final ReentrantLock mutex = new ReentrantLock();

    private final String clientId;
    private final String authKey;
    private final RetryPolicy<Response<AuthResponse>> retryPolicy;
    private final AuthApi api;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final int authRetryTimeoutMs;

    @PostConstruct
    public void scheduleAuth() {
        taskScheduler.schedule(new AuthUpdater(mutex), Instant.now());
    }

    @Nullable
    private String authHeader = null;
    @Nullable

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    @Synchronized("mutex")
    public Either<AppFailure, String> getAuthHeader() {
        if (authHeader == null) {
            return Either.left(new IntegrationFailure(
                    IntegrationFailure.Code.GIGA_CHAT_INTEGRATION_AUTH_FAILURE,
                    getClass().getName(),
                    "No active auth header available"
            ));
        }

        return Either.right(authHeader);
    }

    private AppFailure toFailure(@Nullable Throwable cause) {
        return toFailure(
                IntegrationFailure.Code.GIGA_CHAT_INTEGRATION_AUTH_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "giga-auth", "integration.method", "authenticate"},
            histogram = true,
            percentiles ={0.9, 0.95, 0.99}
    )
    private Either<AppFailure, AuthResponse> authenticate() {
        val uuid = UUID.randomUUID().toString();
        log.info("Auth request for client {} with uuid: {}", clientId, uuid);
        val call = api.authenticate(
                UUID.randomUUID().toString(),
                "Basic: " + authKey,
                AuthScope.GIGA_CHAT_API_PERS
        );

        return Try.of(() -> FailsafeCall.with(retryPolicy).compose(call).execute())
                .map(this::onResponse)
                .onSuccess(res ->
                    log.info(
                            "Got authentication header for client {}, expires at {} ({})",
                            clientId,
                            res.expiresAt(),
                            Instant.ofEpochMilli(res.expiresAt())
                    )
                )
                .onFailure(cause -> log.error("Error authenticating:", cause))
                .toEither()
                .mapLeft(this::toFailure);
    }
}
