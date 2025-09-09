package pro.ra_tech.giga_ai_agent.integration.config.hfs;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.HfsServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.hfs.api.HfsApi;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class HfsConfig extends BaseIntegrationConfig {
    public static final String HFS_SERVICE = "HFS";

    @Bean
    public HfsService hfsService(HfsProps props, MeterRegistry registry) {
        val client = buildOkHttpClient(props.requestTimeoutMs());

        val api = new Retrofit.Builder()
                .baseUrl(props.apiBaseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        return new HfsServiceImpl(
                api.create(HfsApi.class),
                buildRequestMonitoringDto(
                        registry,
                        HFS_SERVICE,
                        "upload",
                        props.maxRetries(),
                        props.retryTimeoutMs()
                ),
                buildRequestMonitoringDto(
                        registry,
                        HFS_SERVICE,
                        "download",
                        props.maxRetries(),
                        props.retryTimeoutMs()
                )
        );
    }
}
