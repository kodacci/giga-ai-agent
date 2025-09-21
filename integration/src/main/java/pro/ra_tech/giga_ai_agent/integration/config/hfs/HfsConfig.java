package pro.ra_tech.giga_ai_agent.integration.config.hfs;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.ra_tech.giga_ai_agent.integration.api.HfsService;
import pro.ra_tech.giga_ai_agent.integration.config.BaseIntegrationConfig;
import pro.ra_tech.giga_ai_agent.integration.impl.HfsServiceImpl;
import pro.ra_tech.giga_ai_agent.integration.rest.hfs.api.HfsApi;
import pro.ra_tech.giga_ai_agent.integration.util.HttpRequestMonitoringDto;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(HfsProps.class)
public class HfsConfig extends BaseIntegrationConfig {
    public static final String HFS_SERVICE = "HFS";

    private <T> HttpRequestMonitoringDto<T> buildMonitoringDto(MeterRegistry registry, HfsProps props, String method) {
        return buildRequestMonitoringDto(
                registry,
                HFS_SERVICE,
                method,
                props.maxRetries(),
                props.retryTimeoutMs()
        );
    }

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
                "Basic " + Base64.getEncoder().encodeToString(
                        (props.user() + ":" + props.password()).getBytes(StandardCharsets.UTF_8)
                ),
                buildMonitoringDto(registry, props, "upload"),
                buildMonitoringDto(registry, props, "download"),
                buildMonitoringDto(registry, props, "delete"),
                buildMonitoringDto(registry, props, "comment")
        );
    }
}
