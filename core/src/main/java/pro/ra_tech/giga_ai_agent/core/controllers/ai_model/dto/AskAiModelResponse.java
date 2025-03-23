package pro.ra_tech.giga_ai_agent.core.controllers.ai_model.dto;

import lombok.val;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelChoice;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelChoiceMessage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record AskAiModelResponse(List<String> messages, AiModelUsage usage) {
    public static AskAiModelResponse of(AiModelAnswerResponse answer) {
        val messages = answer.choices().stream()
                .map(AiModelChoice::message)
                .map(
                        message -> Optional.ofNullable(message)
                                .map(AiModelChoiceMessage::content)
                                .orElse(null)
                )
                .filter(Objects::nonNull)
                .toList();

        return new AskAiModelResponse(messages, AiModelUsage.of(answer.usage()));
    }
}
