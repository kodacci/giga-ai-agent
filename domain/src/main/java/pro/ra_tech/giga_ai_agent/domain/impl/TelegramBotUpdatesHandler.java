package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import pro.ra_tech.giga_ai_agent.domain.config.TelegramBotProps;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelUsage;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.MessageParseMode;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramUser;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.MessageEntityType.MENTION;
import static pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramChatType.PRIVATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBotUpdatesHandler implements Runnable {
    private final BlockingQueue<BotUpdate> botUpdatesQueue;
    private final TelegramBotService botService;
    private final GigaChatService gigaService;
    private final TelegramBotProps props;

    private String findPrompt(TelegramMessage message, String userName) {
        val text = message.text();
        if (message.entities() == null || message.entities().isEmpty() || text == null || userName.isEmpty()) {
            return "";
        }

        log.info("Processing message entities: {}", message.entities());

        return message.entities().stream()
                .filter(entity -> entity.type() == MENTION)
                .filter(entity -> userName.equals(text.substring(entity.offset() + 1, entity.length())))
                .findAny()
                .map(entity -> text.substring(entity.offset() + entity.length()))
                .map(String::trim)
                .orElse("");
    }

    private AiModelUsage sendAnswerParts(AiModelAnswerResponse res, long chatId, int replyTo) {
        res.choices().stream()
                .filter(choice -> choice.message() != null)
                .forEach(
                        choice -> botService.sendMessage(chatId, choice.message().content(), replyTo)
                                .peekLeft(failure -> log.error("Error sending reply: {}", failure.getMessage()))
                );

        return res.usage();
    }

    private String toUsageMessage(AiModelUsage usage) {
        return String.format(
                "*Потрачено*: \nВходящее сообщение - %d\nНа генерацию моделью - %d\nНа кэш - %d\n\n*Итого* - %d токенов",
                usage.promptTokens(),
                usage.completionTokens(),
                usage.precachedPromptTokens(),
                usage.totalTokens()
        );
    }

    private void sendResponse(TelegramMessage message, String prompt, String user) {
        val id = UUID.randomUUID().toString();
        val chatId = message.chat().id();
        val replyTo = message.messageId();
        log.info("Asking AI model rq: {}, session: {}, with: {}", id, user, prompt);

        gigaService.askModel(id, props.aiModelType(), prompt, user)
                .map(res -> sendAnswerParts(res, chatId, replyTo))
                .flatMap(usage -> botService.sendMessage(chatId, toUsageMessage(usage), null, MessageParseMode.MARKDOWN))
                .peekLeft(failure -> log.error("Error while asking model and sending answer: {}", failure.getMessage()));
    }

    @Override
    public void run() {
        log.info("Started bot updates handler");

        val name = botService.getMe().fold(
                failure -> {
                    log.error("Error getting bot info: {}", failure.getMessage());
                    return "";
                },
                user -> Optional.ofNullable(user.userName()).orElse("")
        );

        log.info("Received bot name: {}", name);

        for (;;) {
            try {
                val update = botUpdatesQueue.take();
                log.info("Got message from {}: {}",
                        Optional.ofNullable(update.message())
                                .map(TelegramMessage::from)
                                .map(user -> String.format("%s (%s)", user.userName(), (user.firstName())))
                                .orElse("unknown"),
                        Optional.ofNullable(update.message()).map(TelegramMessage::text).orElse("")
                );

                log.info("Update object: {}", update);

                val message = update.message();
                if (message != null) {
                    val user = Optional.ofNullable(update.user()).map(TelegramUser::userName).orElse(null);
                    if (message.chat().type() == PRIVATE) {
                        sendResponse(message, message.text(), user);
                        continue;
                    }

                    val prompt = findPrompt(message, name);
                    if(!prompt.isEmpty()) {
                        sendResponse(message, prompt, user);
                    }
                }
            } catch (InterruptedException ex) {
                log.info("Interrupting bot updates handler");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
