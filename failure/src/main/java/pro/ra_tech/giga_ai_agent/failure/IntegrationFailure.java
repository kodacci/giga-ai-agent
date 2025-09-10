package pro.ra_tech.giga_ai_agent.failure;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;

public class IntegrationFailure extends AbstractFailure {
    private static final String DETAIL = "Integration service API call error";
    private final Code code;

    public IntegrationFailure(Code code, String source, @Nullable String message) {
        super(source, message);
        this.code = code;
    }

    public IntegrationFailure(Code code, String source, Throwable cause) {
        super(source, cause);
        this.code = code;
    }

    @Override
    public String getCode() {
        return code.toString();
    }

    @Override
    public String getDetail() {
        return DETAIL;
    }

    @RequiredArgsConstructor
    public enum Code {
        GIGA_CHAT_INTEGRATION_AUTH_FAILURE("GIGA_CHAT_INTEGRATION_AUTH_FAILURE"),
        GIGA_CHAT_INTEGRATION_FAILURE("GIGA_CHAT_INTEGRATION_FAILURE"),
        TELEGRAM_BOT_INTEGRATION_FAILURE("TELEGRAM_BOT_INTEGRATION_FAILURE"),
        LLM_TEXT_PROCESSOR_FAILURE("LLM_TEXT_PROCESSOR_FAILURE"),
        YA_GPT_INTEGRATION_AUTH_FAILURE("YA_GPT_INTEGRATION_AUTH_FAILURE"),
        YA_GPT_INTEGRATION_FAILURE("YA_GPT_INTEGRATION_FAILURE"),
        HFS_INTEGRATION_FAILURE("HFS_INTEGRATION_FAILURE");

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }
}
