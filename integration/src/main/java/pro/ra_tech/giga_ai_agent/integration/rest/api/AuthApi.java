package pro.ra_tech.giga_ai_agent.integration.rest.api;

import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthScope;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.UUID;

public interface AuthApi {
    @FormUrlEncoded
    @POST("/api/v2/oauth")
    Call<AuthResponse> authenticate(
            @Header("RqUID") String rqUid,
            @Header("Authorization") String token,
            @Field("scope")AuthScope scope
    );
}
