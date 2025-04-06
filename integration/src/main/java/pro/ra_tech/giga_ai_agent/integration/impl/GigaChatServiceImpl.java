package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.annotation.Timed;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiAskMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAskRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiRole;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingData;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingModel;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingUsage;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetAiModelsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetBalanceResponse;
import retrofit2.Response;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.DoubleStream;

@Slf4j
public class GigaChatServiceImpl extends BaseRestService implements GigaChatService {
    private final GigaAuthService authService;
    private final GigaChatApi gigaApi;
    private final RetryPolicy<Response<GetAiModelsResponse>> getAiModelsPolicy;
    private final RetryPolicy<Response<AiModelAnswerResponse>> askAiModelPolicy;
    private final RetryPolicy<Response<GetBalanceResponse>> getBalancePolicy;
    private final RetryPolicy<Response<CreateEmbeddingsResponse>> createEmbeddingPolicy;
    private final ReentrantLock mutex = new ReentrantLock();
    private final Random random = new Random();

    public GigaChatServiceImpl(
            GigaAuthService authService,
            GigaChatApi gigaApi,
            int maxRetries
    ) {
        this.authService = authService;
        this.gigaApi = gigaApi;

        getAiModelsPolicy = buildPolicy(maxRetries);
        askAiModelPolicy = buildPolicy(maxRetries);
        getBalancePolicy = buildPolicy(maxRetries);
        createEmbeddingPolicy = buildPolicy(maxRetries);

        log.info("Created Giga Chat service for client {}", authService.getClientId());
    }

    private AppFailure toFailure(Throwable cause) {
        return toFailure(
                IntegrationFailure.Code.GIGA_CHAT_INTEGRATION_FAILURE,
                getClass().getName(),
                cause
        );
    }

    @Override
    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "giga-chat", "integration.method", "get-models"},
            histogram = true,
            percentiles ={0.9, 0.95, 0.99}
    )
    @Synchronized("mutex")
    public Either<AppFailure, GetAiModelsResponse> listModels() {
        log.info("Getting ai models list");

        return authService.getAuthHeader()
                .flatMap(
                        authHeader -> sendRequest(
                                getAiModelsPolicy,
                                gigaApi.getAiModels(authHeader),
                                this::toFailure
                        )
                )
                .peekLeft(failure -> log.error("Error getting models list:", failure.getCause()));
    }

    @Override
    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "giga-chat", "integration.method", "chat-completions"},
            histogram = true,
            percentiles ={0.9, 0.95, 0.99}
    )
    @Synchronized("mutex")
    public Either<AppFailure, AiModelAnswerResponse> askModel(
            String rqUid,
            AiModelType model,
            String prompt,
            @Nullable String sessionId
    ) {
        log.info("Asking model {} with prompt: `{}`", model, prompt);
        val request = AiModelAskRequest.builder()
                .model(model)
                .messages(List.of(
                        new AiAskMessage(
                                AiRole.USER,
                                prompt,
                                null,
                                null
                        )
                ))
                .build();

        log.info("Sending chat completions request {}", request);
        return authService.getAuthHeader()
                .flatMap(
                        authHeader -> sendRequest(
                                askAiModelPolicy,
                                gigaApi.askModel(
                                        authHeader,
                                        authService.getClientId(),
                                        rqUid,
                                        sessionId,
                                        request
                                ),
                                this::toFailure
                        )
                )
                .peek(res -> log.info("Successfully got model response on {}: {}", rqUid, res))
                .peekLeft(failure -> log.error("Error asking model {}: ", model, failure.getCause()));
    }

    @Override
    @Synchronized("mutex")
    public Either<AppFailure, GetBalanceResponse> getBalance(@Nullable String sessionId) {
        return authService.getAuthHeader()
                .flatMap(auth -> sendRequest(
                        getBalancePolicy,
                        gigaApi.getBalance(auth, UUID.randomUUID().toString(), sessionId),
                        this::toFailure
                ));
    }

    @Override
    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "giga-chat", "integration.method", "create-embeddings"},
            histogram = true,
            percentiles ={0.9, 0.95, 0.99}
    )
    @Synchronized("mutex")
    public Either<AppFailure, CreateEmbeddingsResponse> createEmbeddings(List<String> input) {
        log.info("Creating new embeddings for {} inputs", input.size());

//        val data = Stream.ofAll(input)
//                .zipWithIndex()
//                .map(item -> new EmbeddingData(
//                        "object",
//                        DoubleStream.generate(random::nextDouble).limit(16).boxed().toList(),
//                        item._2(),
//                        new EmbeddingUsage(item._1().length())
//                ))
//                .toJavaList();
//
//        return Either.right(new CreateEmbeddingsResponse("list", data, EmbeddingModel.EMBEDDINGS));

        return authService.getAuthHeader()
                .flatMap(auth -> sendRequest(
                        createEmbeddingPolicy,
                        gigaApi.createEmbeddings(auth, new CreateEmbeddingsRequest(EmbeddingModel.EMBEDDINGS, input)),
                        this::toFailure
                ));
    }
}
