package pro.ra_tech.giga_ai_agent.integration.rest.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

public record SendMessageRequest(
        @JsonProperty("chat_id") long chatId,
        @JsonProperty("text") String text,
        @JsonProperty("disable_notification") @Nullable Boolean disableNotification,
        @JsonProperty("reply_parameters") @Nullable ReplyParameters replyParameters
) {
}
