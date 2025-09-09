package pro.ra_tech.giga_ai_agent.integration.rest.hfs.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface HfsApi {
    @PUT("/{folder}/{file}")
    @Headers("Content-Type: application/octet-stream")
    Call<Void> upload(
            @Path("folder") String folder,
            @Path("file") String fileName,
            @Body byte[] data
    );

    @GET("/{folder}/{file}")
    Call<ResponseBody> download(
            @Path("folder") String folder,
            @Path("file") String fileName
    );
}
