package pro.ra_tech.giga_ai_agent.domain.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.scheduling.annotation.Scheduled;
import pro.ra_tech.giga_ai_agent.integration.api.GigaChatService;
import pro.ra_tech.giga_ai_agent.integration.rest.giga.model.GetBalanceResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RequiredArgsConstructor
public class BalanceGaugeService {
    private final GigaChatService gigaChatService;
    private final Map<String, AtomicLong> aiModelsBalances;

    private void updateGauges(GetBalanceResponse data) {
        data.balance().forEach(item -> {
            val balance = aiModelsBalances.get(item.usage());
            if (balance != null) {
                balance.set(item.value());
            } else {
                log.warn("Unexpected balance model name: {}", item.usage());
            }
        });
    }

    @Scheduled(fixedRate = 15L, timeUnit = TimeUnit.SECONDS)
    public void updateBalanceGauge() {
        gigaChatService.getBalance()
                .peek(this::updateGauges)
                .peekLeft(failure -> log.warn("Error getting balance for Giga Chat:", failure.getCause()));
    }
}
