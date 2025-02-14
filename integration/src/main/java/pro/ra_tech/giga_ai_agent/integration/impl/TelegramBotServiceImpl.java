package pro.ra_tech.giga_ai_agent.integration.impl;

import dev.failsafe.RetryPolicy;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.IntegrationFailure;
import pro.ra_tech.giga_ai_agent.integration.api.TelegramBotService;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.api.TelegramBotApi;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.GetUpdatesRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.ReplyParameters;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.SendMessageRequest;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramApiResponse;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;
import retrofit2.Call;
import retrofit2.Response;

import java.util.List;

@Slf4j
public class TelegramBotServiceImpl extends BaseRestService implements TelegramBotService {
    private final TelegramBotApi api;
    private final TelegramBotApi pollApi;
    private final int updateLimit;
    private final int updateTimeout;
    private final RetryPolicy<Response<TelegramApiResponse<List<BotUpdate>>>> getUpdatesPolicy;
    private final RetryPolicy<Response<TelegramApiResponse<TelegramMessage>>> sendMessagePolicy;

    private int offset;

    public TelegramBotServiceImpl(
            TelegramBotApi api,
            TelegramBotApi pollApi,
            int maxRetries,
            int updateLimit,
            int updateTimeout
    ) {
        this.api = api;
        this.pollApi = pollApi;
        this.updateLimit = updateLimit;
        this.updateTimeout = updateTimeout;

        getUpdatesPolicy = buildPolicy(maxRetries);
        sendMessagePolicy = buildPolicy(maxRetries);
    }

    private AppFailure toFailure(Throwable cause) {
        return toFailure(IntegrationFailure.Code.TELEGRAM_BOT_INTEGRATION_FAILURE, getClass().getName(), cause);
    }

    private <T> Either<AppFailure, T> sendTelegramRequest(
            RetryPolicy<Response<TelegramApiResponse<T>>> policy,
            Call<TelegramApiResponse<T>> call
    ) {
        return sendRequest(policy, call, this::toFailure)
                .flatMap(res -> {
                    if (res.ok() && res.result() != null) {
                        return Either.right(res.result());
                    }

                    return Either.left(new IntegrationFailure(
                            IntegrationFailure.Code.TELEGRAM_BOT_INTEGRATION_FAILURE,
                            getClass().getName(),
                            res.ok() ? "Empty response" : res.error()
                    ));
                });
    }

    @Override
    @Counted
    public Either<AppFailure, List<BotUpdate>> getUpdates() {
        return sendTelegramRequest(
                getUpdatesPolicy,
                pollApi.getUpdates(new GetUpdatesRequest(offset, updateLimit, updateTimeout, null))
        )
                .peek(res -> {
                    if (!res.isEmpty()) {
                        offset = res.get(res.size() - 1).updateId() + 1;
                        log.info("Setting offset for updates at {}", offset);
                    }
                })
                .peekLeft(failure -> log.error("Error getting bot updates: {}", failure.getMessage()));
    }

    @Override
    @Timed(
            value = "integration.call",
            extraTags = {"integration.service", "telegram-bot", "integration.method", "sendMessage"},
            histogram = true,
            percentiles = {0.9, 0.95, 0.99}
    )
    public Either<AppFailure, TelegramMessage> sendMessage(long chatId, String text, Integer replyMessageId) {
        val reply = replyMessageId == null ? null : new ReplyParameters(replyMessageId);
        val request = new SendMessageRequest(chatId, text, false, reply);

        return sendTelegramRequest(sendMessagePolicy, api.sendMessage(request));
    }
}
