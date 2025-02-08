package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import dev.failsafe.retrofit.FailsafeCall;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.rest.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthScope;
import pro.ra_tech.giga_ai_agent.integration.rest.model.GetAiModelsResponse;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class GigaChatServiceImpl implements GigaChatService {
    private static class ApiException extends RuntimeException {
        public ApiException(String message) {
            super(message);
        }
    }

    private final GigaChatApi gigaApi;
    private final FailsafeCall<AuthResponse> authFailsafe;
    private final RetryPolicy<Response<GetAiModelsResponse>> getAiModelsPolicy;

    private String authHeader = null;
    private Instant authExpiresAt = Instant.now();

    public GigaChatServiceImpl(
            String clientId,
            String authKey,
            AuthApi authApi,
            GigaChatApi gigaApi,
            int maxRetries
    ) {
        this.gigaApi = gigaApi;

        val call = authApi.authenticate(UUID.randomUUID().toString(), "Basic: " + authKey, AuthScope.GIGACHAT_API_PERS);
        val policy = RetryPolicy.<Response<AuthResponse>>builder().withMaxRetries(maxRetries).build();
        authFailsafe = FailsafeCall.with(policy).compose(call);

        getAiModelsPolicy = buildPolicy(maxRetries);

        log.info("Created Giga Chat service for client {}", clientId);
    }

    private static <T> RetryPolicy<Response<T>> buildPolicy(int maxRetries) {
        return RetryPolicy.<Response<T>>builder().withMaxRetries(maxRetries).build();
    }

    private AppFailure toFailure(Throwable cause) {
        return new IntegrationFailure(
                IntegrationFailure.Code.GIGA_CHAT_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    private void authenticate() {
        Try.of(authFailsafe::execute)
                .map(this::onResponse)
                .onSuccess(res -> {
                    authHeader = "Bearer " + res.accessToken();
                    authExpiresAt = Instant.ofEpochSecond(res.expiresAt());
                    log.info("Got authentication header, expires at {}", authExpiresAt);
                })
                .onFailure(cause -> log.error("Error authenticating:", cause));
    }

    private void checkIfAuthenticated() {
        if (authHeader == null || authExpiresAt.isAfter(Instant.now().plus(1, ChronoUnit.SECONDS))) {
            log.info("Authenticating");
            authenticate();
        }
    }

    private <R> R onResponse(Response<R> response) {
        if (response.isSuccessful()) {
            return response.body();
        }

        try (val body = response.errorBody()) {
            val message = body == null ? "Unknown error" : body.string();
            log.error("API request error with code: {} and body: {}", response.code(), message);
            throw new ApiException(String.format("Bad response with code %d, body: %s", response.code(), message));
        } catch (IOException e) {
            throw new ApiException("Bad response with code " + Integer.toString(response.code()));
        }
    }

    private <R> Either<AppFailure, R> sendRequest(RetryPolicy<Response<R>> retryPolicy, Call<R> call) {
        checkIfAuthenticated();

        return Try.of(() -> FailsafeCall.with(retryPolicy).compose(call).execute())
                .map(this::onResponse)
                .toEither()
                .mapLeft(this::toFailure);
    }

    @Override
    public Either<AppFailure, GetAiModelsResponse> listModels() {
        log.info("Getting ai models list");
        return sendRequest(getAiModelsPolicy, gigaApi.getAiModels(authHeader));
    }

    @Override
    public Either<AppFailure, AiModelAnswerResponse> askModel(String prompt) {
        return null;
    }
}
