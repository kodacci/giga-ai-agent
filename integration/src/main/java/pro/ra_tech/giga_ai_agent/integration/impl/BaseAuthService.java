package pro.ra_tech.giga_ai_agent.integration.impl;

import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.TaskScheduler;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.AuthService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RequiredArgsConstructor
public abstract class BaseAuthService extends BaseRestService implements AuthService {
    protected static final int EXPIRES_TIMEOUT_CORRECTION_SEC = 5;

    protected record AuthTokenDto(String token, Instant expiresAt) {}

    @RequiredArgsConstructor
    private class AuthUpdater implements Runnable {
        private final ReentrantLock mutex;

        @Synchronized("mutex")
        public void run() {
            acquireToken().peek(tokenData -> {
                        authHeader = "Bearer " + tokenData.token();
                        taskScheduler.schedule(
                                new BaseAuthService.AuthUpdater(mutex),
                                tokenData.expiresAt().minus(EXPIRES_TIMEOUT_CORRECTION_SEC, ChronoUnit.SECONDS)
                        );
                    })
                    .peekLeft(failure -> {
                        authHeader = null;
                        taskScheduler.schedule(
                                new BaseAuthService.AuthUpdater(mutex),
                                Instant.now().plus(authRetryTimeoutMs, ChronoUnit.MILLIS)
                        );
                    });
        }
    }

    private final ReentrantLock mutex = new ReentrantLock();
    private final TaskScheduler taskScheduler;
    private final int authRetryTimeoutMs;
    private final IntegrationFailure.Code failureCode;

    protected abstract Either<AppFailure, AuthTokenDto> acquireToken();

    @PostConstruct
    public void scheduleAuth() {
        taskScheduler.schedule(new BaseAuthService.AuthUpdater(mutex), Instant.now());
    }

    @Nullable
    private String authHeader = null;

    @Override
    @Synchronized("mutex")
    public Either<AppFailure, String> getAuthHeader() {
        if (authHeader == null) {
            return Either.left(new IntegrationFailure(
                    failureCode,
                    getClass().getName(),
                    "No active auth header available"
            ));
        }

        return Either.right(authHeader);
    }
}
