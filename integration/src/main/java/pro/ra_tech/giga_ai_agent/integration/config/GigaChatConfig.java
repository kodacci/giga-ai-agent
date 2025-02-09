package pro.ra_tech.giga_ai_agent.integration.config;

import lombok.val;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.impl.GigaChatServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.api.AuthApi;
import pro.ra_tech.giga_ai_agent.integration.rest.api.GigaChatApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
public class GigaChatConfig {
    @Bean
    public GigaChatService gigaChatService(GigaChatProps props) {
        val client = new OkHttpClient.Builder()
                .callTimeout(props.requestTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();

        val authApi = new Retrofit.Builder()
                .baseUrl(props.authApiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        val gigaApi = new Retrofit.Builder()
                .baseUrl(props.apiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        return new GigaChatServiceImpl(
                props.clientId(),
                props.authKey(),
                authApi.create(AuthApi.class),
                gigaApi.create(GigaChatApi.class),
                props.maxRetries()
        );
    }
}
