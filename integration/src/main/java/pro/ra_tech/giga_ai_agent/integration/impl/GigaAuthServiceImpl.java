package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import dev.failsafe.retrofit.FailsafeCall;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.rest.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthScope;
import retrofit2.Response;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class GigaAuthServiceImpl extends BaseService implements GigaAuthService {
    private final String clientId;
    private final String authKey;
    private final RetryPolicy<Response<AuthResponse>> retryPolicy;
    private final AuthApi api;

    private String authHeader = null;
    private Instant authExpiresAt = null;

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    @Synchronized
    public Either<AppFailure, String> getAuthHeader() {
        if (authHeader == null || authExpiresAt.isBefore(Instant.now().plus(1, ChronoUnit.SECONDS))) {
            return authenticate().map(res -> authHeader);
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

    private Either<AppFailure, AuthResponse> authenticate() {
        val uuid = UUID.randomUUID().toString();
        log.info("Auth request for client {} with uuid: {}", clientId, uuid);
        val call = api.authenticate(
                UUID.randomUUID().toString(),
                "Basic: " + authKey,
                AuthScope.GIGACHAT_API_PERS
        );

        return Try.of(() -> FailsafeCall.with(retryPolicy).compose(call).execute())
                .map(this::onResponse)
                .onSuccess(res -> {
                    authHeader = "Bearer " + res.accessToken();
                    authExpiresAt = Instant.ofEpochMilli(res.expiresAt());
                    log.info(
                            "Got authentication header for client {}, expires at {} ({})",
                            clientId,
                            res.expiresAt(),
                            authExpiresAt
                    );
                })
                .onFailure(cause -> log.error("Error authenticating:", cause))
                .toEither()
                .mapLeft(this::toFailure);
    }
}
