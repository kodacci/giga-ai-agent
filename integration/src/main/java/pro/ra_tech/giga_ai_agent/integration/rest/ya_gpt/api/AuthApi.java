package pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.api;

import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AuthRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/iam/v1/tokens")
    Call<AuthResponse> authenticate(@Body AuthRequest request);
}
