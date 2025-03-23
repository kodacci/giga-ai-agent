package pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.api;

import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.model.SplitTextRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.model.SplitTextResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface LlmTextProcessorApi {
    @POST("api/v1/chunkers/recursive/split-text")
    @Headers("Content-type: application/json")
    Call<SplitTextResponse> splitText(@Body SplitTextRequest request);
}
