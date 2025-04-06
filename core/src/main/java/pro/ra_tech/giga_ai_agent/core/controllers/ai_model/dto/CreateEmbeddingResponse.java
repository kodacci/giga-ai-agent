package pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.CreateEmbeddingsResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingData;

import java.util.List;

public record CreateEmbeddingResponse(
        @NotNull
        List<Double> embedding,
        @Min(0)
        @NotNull
        int usedTokens
) {
    public static CreateEmbeddingResponse of(CreateEmbeddingsResponse data) {
        return new CreateEmbeddingResponse(
                data.data().stream().findAny().map(EmbeddingData::embedding).orElse(List.of()),
                data.data().stream().findAny().map(item -> item.usage().promptTokens()).orElse(0)
        );
    }
}
