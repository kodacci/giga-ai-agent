package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AuthResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AuthScope;
import retrofit2.Response;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public class GigaAuthServiceImpl extends BaseAuthService implements GigaAuthService {

    private final String clientId;
    private final String authKey;
    private final RetryPolicy<Response<AuthResponse>> retryPolicy;
    private final AuthApi api;
    private final Timer authTimer;
    private final Counter auth4xxCounter;
    private final Counter auth5xxCounter;

    public GigaAuthServiceImpl(
            String clientId,
            String authKey,
            RetryPolicy<Response<AuthResponse>> retryPolicy,
            AuthApi api,
            TaskScheduler taskScheduler,
            int authRetryTimeoutMs,
            Timer authTimer,
            Counter auth4xxCounter,
            Counter auth5xxCounter
    ) {
        super(taskScheduler, authRetryTimeoutMs, IntegrationFailure.Code.YA_GPT_INTEGRATION_AUTH_FAILURE);

        this.clientId = clientId;
        this.authKey = authKey;
        this.api = api;
        this.retryPolicy = retryPolicy;
        this.authTimer = authTimer;
        this.auth4xxCounter = auth4xxCounter;
        this.auth5xxCounter = auth5xxCounter;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    protected Either<AppFailure, AuthTokenDto> acquireToken() {
        val uuid = UUID.randomUUID().toString();
        log.info("Auth request for client {} with uuid: {}", clientId, uuid);
        val call = api.authenticate(
                UUID.randomUUID().toString(),
                "Basic: " + authKey,
                AuthScope.GIGA_CHAT_API_PERS
        );

        return Try.of(() -> Failsafe.with(retryPolicy).get(() -> authTimer.recordCallable(call::execute)))
                .map(res -> onResponse(res, auth4xxCounter, auth5xxCounter))
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
                .mapLeft(this::toFailure)
                .map(res -> new AuthTokenDto(
                        res.accessToken(),
                        Instant.ofEpochMilli(res.expiresAt())
                ));
    }

    private AppFailure toFailure(@Nullable Throwable cause) {
        return GigaAuthServiceImpl.this.toFailure(
                IntegrationFailure.Code.GIGA_CHAT_INTEGRATION_AUTH_FAILURE,
                getClass().getName(),
                cause
        );
    }
}
