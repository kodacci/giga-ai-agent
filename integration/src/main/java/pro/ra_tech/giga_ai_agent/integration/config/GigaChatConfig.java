package pro.ra_tech.giga_ai_agent.integration.config;

import dev.failsafe.RetryPolicy;
import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.GigaAuthService;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaAuthServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaChatServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.api.GigaChatApi;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AuthResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class GigaChatConfig {
    @Bean
    public OkHttpClient gigaHttpClient(GigaChatProps props) {
        return new OkHttpClient.Builder()
                .callTimeout(props.requestTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public GigaAuthService gigaAuthenticator(OkHttpClient gigaHttpClient, GigaChatProps props) {
        val authApi = new Retrofit.Builder()
                .baseUrl(props.authApiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(gigaHttpClient)
                .build();

        val retryPolicy = RetryPolicy.<Response<AuthResponse>>builder().withMaxRetries(props.maxRetries()).build();

        return new GigaAuthServiceImpl(props.clientId(), props.authKey(), retryPolicy, authApi.create(AuthApi.class));
    }

    @Bean
    public GigaChatService gigaChatService(OkHttpClient client, GigaChatProps props, GigaAuthService authService) {
        val gigaApi = new Retrofit.Builder()
                .baseUrl(props.apiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        return new GigaChatServiceImpl(
                authService,
                gigaApi.create(GigaChatApi.class),
                props.maxRetries()
        );
    }
}
