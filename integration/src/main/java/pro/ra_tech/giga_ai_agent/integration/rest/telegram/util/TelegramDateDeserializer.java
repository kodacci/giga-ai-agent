package pro.ra_tech.giga_ai_agent.integration.rest.telegram.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

public class TelegramDateDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        return Instant.ofEpochSecond(parser.getValueAsInt());
    }
}
