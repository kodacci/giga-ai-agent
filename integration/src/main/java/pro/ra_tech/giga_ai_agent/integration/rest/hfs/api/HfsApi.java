package pro.ra_tech.giga_ai_agent.integration.rest.hfs.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import pro.ra_tech.giga_ai_agent.integration.rest.hfs.model.CommentRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface HfsApi {
    @PUT("/{folder}/{file}")
    @Headers("Content-Type: application/octet-stream")
    Call<Void> upload(
            @Path("folder") String folder,
            @Path("file") String fileName,
            @Header("Authorization") String authHeader,
            @Body RequestBody data
    );

    @GET("/{folder}/{file}")
    Call<ResponseBody> download(
            @Path("folder") String folder,
            @Path("file") String fileName,
            @Header("Authorization") String authHeader
    );

    @DELETE("/{folder}/{file}")
    Call<Void> delete(
            @Path("folder") String folder,
            @Path("file") String fileName,
            @Header("Authorization") String auth
    );

    @POST("/~/api/comment")
    Call<Void> comment(
            @Header("Authorization") String authHeader,
            @Body CommentRequest request
    );
}
