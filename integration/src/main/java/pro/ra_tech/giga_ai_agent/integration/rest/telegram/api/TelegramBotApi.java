package pro.ra_tech.giga_ai_agent.integration.rest.telegram.api;

import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.GetUpdatesRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.SendMessageRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramApiResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramUser;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.util.List;

public interface TelegramBotApi {
    @POST("getUpdates")
    @Headers("Content-type: application/json")
    Call<TelegramApiResponse<List<BotUpdate>>> getUpdates(
            @Body GetUpdatesRequest request
    );

    @POST("getMe")
    @Headers("Content-type: application/json")
    Call<TelegramApiResponse<TelegramUser>> getMe();

    @POST("sendMessage")
    @Headers("Content-type: application/json")
    Call<TelegramApiResponse<TelegramMessage>> sendMessage(@Body SendMessageRequest request);
}
