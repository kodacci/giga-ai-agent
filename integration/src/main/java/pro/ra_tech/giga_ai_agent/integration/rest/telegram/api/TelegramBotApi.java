package pro.ra_tech.giga_ai_agent.integration.rest.telegram.api;

import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.GetUpdatesRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface TelegramBotApi {
    @GET("/bot{token}/getUpdates")
    @Headers("Content-type: application/json")
    Call<Object> getUpdates(
            @Path("token") String token,
            @Body GetUpdatesRequest request
    );
}
