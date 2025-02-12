package pro.ra_tech.giga_ai_agent.core.controllers.dto;

import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelChoice;
import pro.ra_tech.giga_ai_agent.integration.rest.model.AiModelChoiceMessage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AskAiModelResponse(List<String> messages) {
    public static AskAiModelResponse of(AiModelAnswerResponse answer) {
        return new AskAiModelResponse(
                answer.choices().stream()
                        .map(AiModelChoice::message)
                        .map(
                                message -> Optional.ofNullable(message)
                                        .map(AiModelChoiceMessage::content)
                                        .orElse(null)
                        )
                        .filter(Objects::nonNull)
                        .toList()
        );
    }
}
