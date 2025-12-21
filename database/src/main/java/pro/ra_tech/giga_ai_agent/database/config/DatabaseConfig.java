package pro.ra_tech.giga_ai_agent.database.config;

import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.simple.JdbcClient;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.impl.PgObjectToStringListConverter;
import pro.ra_tech.giga_ai_agent.database.repos.impl.SourceRepositoryImpl;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@ComponentScan({
        "pro.ra_tech.giga_ai_agent.database.repos.impl"
})
public class DatabaseConfig {
    @Bean
    public SourceRepository sourceRepository(
            JdbcClient jdbc,
            JsonMapper jsonMapper
    ) {
        val conversionService = DefaultConversionService.getSharedInstance();
        if (conversionService instanceof GenericConversionService) {
            ((GenericConversionService) conversionService).addConverter(new PgObjectToStringListConverter(jsonMapper));
        }
        return new SourceRepositoryImpl(jdbc);
    }
}
