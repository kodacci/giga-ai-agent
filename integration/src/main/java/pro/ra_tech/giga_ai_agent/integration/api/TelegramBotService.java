package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.BotUpdate;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.MessageParseMode;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramMessage;
import pro.ra_tech.giga_ai_agent.integration.rest.telegram.model.TelegramUser;

import java.util.List;

public interface TelegramBotService {
    Either<AppFailure, TelegramUser> getMe();
    Either<AppFailure, List<BotUpdate>> getUpdates();
    Either<AppFailure, TelegramMessage> sendMessage(long chatId, String text, Integer replyMessageId);
    Either<AppFailure, TelegramMessage> sendMessage(long chatId, String text, Integer replyMessageId, MessageParseMode parseMode);
}
