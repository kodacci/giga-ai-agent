package pro.ra_tech.giga_ai_agent.integration.rest.llm_text_processor.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SplitTextResponse(
        @NotNull
        @JsonProperty("chunks")
        List<String> chunks
) {
}
