package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.TaskScheduler;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AuthRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AuthResponse;
import retrofit2.Response;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

@Slf4j
public class YaGptAuthServiceImpl extends BaseAuthService {
    private static final Duration AUTH_TOKEN_EXPIRES_TIMEOUT = Duration.ofHours(1);

    private final AuthApi api;
    private final String oAuthToken;
    private final RetryPolicy<Response<AuthResponse>> retryPolicy;
    private final Timer timer;
    private final Counter auth4xxCounter;
    private final Counter auth5xxCounter;

    public YaGptAuthServiceImpl(
            AuthApi api,
            String oAuthToken,
            RetryPolicy<Response<AuthResponse>> retryPolicy,
            Timer authTimer,
            Counter auth4xxCounter,
            Counter auth5xxCounter,
            TaskScheduler taskScheduler,
            int authRetryTimeoutMs
    ) {
        super(taskScheduler, authRetryTimeoutMs, IntegrationFailure.Code.YA_GPT_INTEGRATION_AUTH_FAILURE);

        this.api = api;
        this.oAuthToken = oAuthToken;
        this.retryPolicy = retryPolicy;
        timer = authTimer;
        this.auth4xxCounter = auth4xxCounter;
        this.auth5xxCounter = auth5xxCounter;
    }

    private Instant buildExpiresAt(OffsetDateTime expiresAt) {
        val bestExp = OffsetDateTime.now().plus(AUTH_TOKEN_EXPIRES_TIMEOUT);

        return bestExp.isBefore(expiresAt) ? bestExp.toInstant() : expiresAt.toInstant();
    }

    @Override
    protected Either<AppFailure, AuthTokenDto> acquireToken() {
        return Try.of(
                () -> Failsafe.with(retryPolicy)
                        .get(() -> timer.recordCallable(() -> api.authenticate(new AuthRequest(oAuthToken)).execute()))
        )
                .map(res -> onResponse(res, auth4xxCounter, auth5xxCounter))
                .onSuccess(res -> log.info("Got authentication header"))
                .onFailure(cause -> log.error("Error authenticating:", cause))
                .toEither()
                .mapLeft(this::toFailure)
                .map(res -> new AuthTokenDto(
                        res.iamToken(),
                        buildExpiresAt(res.expiresAt())
                ));
    }

    private AppFailure toFailure(Throwable cause) {
        return toFailure(
            IntegrationFailure.Code.YA_GPT_INTEGRATION_AUTH_FAILURE,
            getClass().getName(),
            cause
        );
    }
}
