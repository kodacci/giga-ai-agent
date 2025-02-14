package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBotUpdatesHandler implements Runnable {
    private final BlockingQueue<BotUpdate> botUpdatesQueue;
    private final TelegramBotService service;

    @Override
    public void run() {
        log.info("Started bot updates handler");

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
                    service.sendMessage(message.chat().id(), "Ha ha! I'v got your message", message.messageId())
                            .peekLeft(failure -> log.error("Error sending reply: {}", failure.getMessage()));
                }
            } catch (InterruptedException ex) {
                log.info("Interrupting bot updates handler");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
