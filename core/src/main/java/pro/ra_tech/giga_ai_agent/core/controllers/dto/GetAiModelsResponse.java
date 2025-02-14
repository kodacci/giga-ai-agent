package pro.ra_tech.giga_ai_agent.core.controllers.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetAiModelsResponse(@NotNull List<String> models) {
    public static GetAiModelsResponse of(pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetAiModelsResponse data) {
        return new GetAiModelsResponse(
                data.data().stream()
                        .map(info -> String.format("%s (%s)", info.id(), info.ownedBy()))
                        .toList()
        );
    }
}
