package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.database.repos.api.EmbeddingRepository;
import pro.ra_tech.giga_ai_agent.database.repos.api.SourceRepository;
import pro.ra_tech.giga_ai_agent.database.repos.model.EmbeddingPersistentData;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelAnswerResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelType;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.AiModelUsage;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingData;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.EmbeddingModel;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetBalanceResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.MessageParseMode;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramUser;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import static pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.MessageEntityType.MENTION;
import static pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramChatType.PRIVATE;

@RequiredArgsConstructor
@Slf4j
public class TelegramBotUpdatesHandler implements Runnable {

    @Data
    private static class ProcessingContext {
        private List<String> usedSources = List.of();
    }

    private final BlockingQueue<BotUpdate> botUpdatesQueue;
    private final TelegramBotService botService;
    private final GigaChatService gigaService;
    private final AiModelType aiModelType;
    private final EmbeddingRepository embeddingRepo;
    private final EmbeddingModel embeddingModel;
    private final SourceRepository sourceRepo;
    private final String promptBase;

    private final DecimalFormat balanceFormatter = new DecimalFormat("###,###,###");

    private String findPrompt(TelegramMessage message, String userName) {
        val text = message.text();
        if (message.entities() == null || message.entities().isEmpty() || text == null || userName.isEmpty()) {
            return "";
        }

        log.info("Processing message entities: {}", message.entities());

        return message.entities().stream()
                .filter(entity -> entity.type() == MENTION)
                .filter(entity -> text.length() > entity.offset() + 1)
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
                "*Потрачено*: %nВходящее сообщение - %d%nНа генерацию моделью - %d%nНа кэш - %d%n%n*Итого* - %d токенов",
                usage.promptTokens(),
                usage.completionTokens(),
                usage.precachedPromptTokens(),
                usage.totalTokens()
        );
    }

    private String toBalanceMessage(GetBalanceResponse res) {
        log.info("Got AI model balance: {}", res);

        return res.balance().stream()
                .filter(balance ->
                        balance.usage().equals(aiModelType.getBalanceName()) ||
                                balance.usage().equals(EmbeddingModel.EMBEDDINGS.getBalanceName())
                )
                .map(balance -> String.format(
                        "*Баланс модели %s:* %s токенов",
                        balance.usage(),
                        balanceFormatter.format(balance.value())
                ))
                .collect(Collectors.joining("\n---------\n"));
    }

    private void attachSources(List<EmbeddingPersistentData> embeddings, ProcessingContext ctx) {
        if (embeddings.isEmpty()) {
            return;
        }

        sourceRepo.getNames(embeddings.stream().map(EmbeddingPersistentData::sourceId).toList())
                .peek(ctx::setUsedSources);
    }

    private void sendResponse(TelegramMessage message, String prompt, String user) {
        val ctx = new ProcessingContext();
        val id = UUID.randomUUID().toString();
        val chatId = message.chat().id();
        val replyTo = message.messageId();
        log.info("Asking AI model rq: {}, session: {}, with: {}", id, user, prompt);

        botService.sendMessage(chatId, "Ассистент-библиотекарь задумался \uD83E\uDD14", replyTo, MessageParseMode.MARKDOWN)
                .peekLeft(failure -> log.error("Error sending informing message: {}", failure.getMessage()));

        gigaService.createEmbeddings(List.of(prompt), embeddingModel)
                .peek(res -> log.info("Created embedding for prompt: {}", res))
                .flatMap(res -> embeddingRepo.vectorSearch(
                        res.data()
                                .stream()
                                .findAny()
                                .map(EmbeddingData::embedding)
                                .orElse(List.of())
                ))
                .peek(embeddings -> log.info("Found embeddings {} in db for prompt {}", embeddings, prompt))
                .peek(embeddings -> attachSources(embeddings, ctx))
                .flatMap(embeddings -> gigaService.askModel(
                        id,
                        aiModelType,
                        promptBase,
                        prompt,
                        user,
                        embeddings.stream()
                                .map(EmbeddingPersistentData::textData)
                                .toList()
                ))
                .map(res -> sendAnswerParts(res, chatId, replyTo))
                .peek(usage -> {
                    if (!ctx.getUsedSources().isEmpty()) {
                        botService.sendMessage(
                                chatId,
                                "*Использовались источники*:\n\uD83D\uDCDA" + String.join(", \n\uD83D\uDCDA", ctx.getUsedSources()),
                                null,
                                MessageParseMode.MARKDOWN
                        );
                    }
                })
                .flatMap(usage -> botService.sendMessage(chatId, toUsageMessage(usage), null, MessageParseMode.MARKDOWN))
                .flatMap(sent -> gigaService.getBalance(null))
                .map(this::toBalanceMessage)
                .map(text -> text.isBlank() ? null : botService.sendMessage(chatId, text, null, MessageParseMode.MARKDOWN))
                .peekLeft(failure -> log.error("Error while asking model and sending answer", failure.getCause()));
    }

    private String getBotName() {
        val name = botService.getMe().fold(
                failure -> {
                    log.error("Error getting bot info: {}", failure.getMessage());
                    return "";
                },
                user -> Optional.ofNullable(user.userName()).orElse("")
        );

        log.info("Received bot name: {}", name);

        return name;
    }

    private void handleUpdate(BotUpdate update, String name) {
        val message = update.message();
        if (message == null) {
            return;
        }

        val user = Optional.ofNullable(update.user()).map(TelegramUser::userName).orElse(null);
        if (message.chat().type() == PRIVATE) {
            sendResponse(message, message.text(), user);
            return;
        } else {
            log.info("Request not in private chat");
        }

        val prompt = findPrompt(message, name);
        if(!prompt.isEmpty()) {
            sendResponse(message, prompt, user);
        } else {
            log.info("Prompt after name {} is empty, skipping", name);
        }
    }

    @Override
    public void run() {
        log.info("Started bot updates handler");

        var name = getBotName();

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

                if (name.isEmpty()) {
                    name = getBotName();
                }

                log.info("Update object: {}", update);
                handleUpdate(update, name);
            } catch (InterruptedException _) {
                log.info("Interrupting bot updates handler");
                Thread.currentThread().interrupt();
                return;
            } catch (Exception ex) {
                log.error("Unexpected exception:", ex);
            }
        }
    }
}
