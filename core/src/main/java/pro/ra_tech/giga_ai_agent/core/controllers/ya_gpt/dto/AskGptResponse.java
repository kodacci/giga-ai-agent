package pro.ra_tech.giga_ai_agent.core.controllers.ya_gpt.dto;

import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.AskModelResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.CompletionAlternative;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.ModelMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.ya_gpt.model.ModelRole;

import java.util.stream.Collectors;

public record AskGptResponse(
        String text
) {
    public static AskGptResponse of(AskModelResponse response) {
        return new AskGptResponse(
                response.result().alternatives().stream()
                        .map(CompletionAlternative::message)
                        .filter(message -> message.role() == ModelRole.ASSISTANT)
                        .map(ModelMessage::text)
                        .collect(Collectors.joining("\n"))
        );
    }
}
