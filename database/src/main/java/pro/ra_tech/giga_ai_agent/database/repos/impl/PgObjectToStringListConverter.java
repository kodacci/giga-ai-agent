package pro.ra_tech.giga_ai_agent.database.repos.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class PgObjectToStringListConverter implements Converter<PGobject, List<String>> {
    private static final String JSON_TYPE = "json";
    private final JsonMapper jsonMapper;

    @Override
    public @Nullable List<String> convert(PGobject source) {
        log.info("Converting PGobject with type {} and value {} to List<String>", source.getType(), source.getValue());
        if (!JSON_TYPE.equals(source.getType())) {
            throw new IllegalArgumentException("Unsupported PGobject type " + source.getType() + " for conversion to List<String>");
        }

        return jsonMapper.readValue(source.getValue(), new TypeReference<>() {});
    }
}
