package pro.ra_tech.giga_ai_agent.integration.rest.util;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.time.OffsetDateTime;

public class OffsetDateTimeConverter extends StdConverter<String, OffsetDateTime> {
    @Override
    public OffsetDateTime convert(String value) {
        return OffsetDateTime.parse(value);
    }
}
