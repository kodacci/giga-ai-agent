package pro.ra_tech.giga_ai_agent.integration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramUser;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBotUpdatesHandler implements Runnable {
    private static final int BATCH_SIZE = 10;
    private final BlockingQueue<BotUpdate> botUpdatesQueue;
    private final TelegramBotService service;

    @Override
    public void run() {
        log.info("Started bot updates handler");
        val updates = new ArrayList<BotUpdate>(BATCH_SIZE);
        for (;;) {
            try {
                botUpdatesQueue.drainTo(updates, BATCH_SIZE);
                log.info("Got {} new updates", updates.size());
                updates.forEach(
                        update -> {
                            log.info("Got message from {}: {}",
                                    Optional.ofNullable(update.message())
                                            .map(TelegramMessage::from)
                                            .map(TelegramUser::firstName)
                                            .orElse("unknown"),
                                    Optional.ofNullable(update.message()).map(TelegramMessage::text).orElse("")
                            );

                            val message = update.message();
                            if (message != null) {
                                service.sendMessage(message.chat().id(), "Ha ha! I'v got your message", message.messageId());
                            }
                        }
                );
            } catch (Exception ex) {
                log.error("Error getting updates from queue", ex);
            } finally {
                updates.clear();
            }
        }
    }
}
