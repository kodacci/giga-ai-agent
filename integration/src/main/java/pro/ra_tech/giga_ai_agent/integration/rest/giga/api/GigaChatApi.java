package pro.ra_tech.giga_ai_agent.integration.rest.giga.api;

import org.springframework.lang.Nullable;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAskRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetAiModelsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetBalanceResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GigaChatApi {
    @GET("/api/v1/models")
    Call<GetAiModelsResponse> getAiModels(@Header("Authorization") String auth);

    @POST("/api/v1/chat/completions")
    Call<AiModelAnswerResponse> askModel(
            @Header("Authorization") String auth,
            @Header("X-Client-ID") String clientId,
            @Header("X-Request-ID") String requestId,
            @Nullable @Header("X-SessionID") String sessionId,
            @Body AiModelAskRequest request
    );

    @GET("/api/v1/balance")
    Call<GetBalanceResponse> getBalance(
            @Header("Authorization") String auth,
            @Header("X-Request-ID") @Nullable String requestId,
            @Header("X-Session-ID") @Nullable String sessionId
    );

    @POST("/api/v1/embeddings")
    Call<CreateEmbeddingsResponse> createEmbeddings(
            @Header("Authorization") String auth,
            @Body CreateEmbeddingsRequest request
    );
}
