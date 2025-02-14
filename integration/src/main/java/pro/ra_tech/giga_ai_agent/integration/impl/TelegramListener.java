package pro.ra_tech.giga_ai_agent.integration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;

import java.util.concurrent.BlockingQueue;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramListener implements Runnable {
    private static final int SLEEP_MS_ON_ERROR = 5000;

    private final TelegramBotService service;
    private final BlockingQueue<BotUpdate> botUpdatesQueue;

    private boolean exit = false;

    @Override
    public void run() {
        log.info("Started telegram listener");

        while (!exit) {
            service.getUpdates()
                    .peek(updates -> {
                        log.info("Got {} new updates", updates.size());
                        updates.forEach(update -> {
                            if (!botUpdatesQueue.offer(update)) {
                                log.warn("Could not add update, message queue is full");
                            }
                        });
                    })
                    .peekLeft(failure -> {
                        log.info("Error getting bot updates:", failure.getCause());
                        try {
                            Thread.sleep(SLEEP_MS_ON_ERROR);
                        } catch (InterruptedException e) {
                            exit = true;
                        }
                    });
        }
    }
}
