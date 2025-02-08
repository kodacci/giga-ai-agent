package pro.ra_tech.giga_ai_agent.integration.rest.api;

import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAskRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.model.GetAiModelsResponse;
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
            @Header("X-Request-ID") String requestID,
            @Header("X-SessionID") String sessionId,
            @Body AiModelAskRequest request
    );
}
