package pro.ra_tech.giga_ai_agent.integration.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GetAiModelsResponse(@JsonProperty("data") List<AiModelInfo> data, @JsonProperty("object") String object) {
}
