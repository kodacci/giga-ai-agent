package pro.ra_tech.giga_ai_agent.integration.config.cloud_ru;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CloudRuProps.class)
public class CloudRuConfig {
    @Bean("cloudRuClient")
    public OpenAIClient openAIClient(CloudRuProps props) {
        return new OpenAIOkHttpClient.Builder()
                .baseUrl(props.modelApiBaseUrl())
                .apiKey(props.modelApiKey())
                .build();
    }
}
