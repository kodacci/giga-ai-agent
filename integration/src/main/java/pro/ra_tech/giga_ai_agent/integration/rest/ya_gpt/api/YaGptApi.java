package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.api;

import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AskModelRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AskModelResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface YaGptApi {
    @POST("/foundationModels/v1/completion")
    Call<AskModelResponse> askModel(
            @Header("Authorization") String auth,
            @Body AskModelRequest request
    );
}
