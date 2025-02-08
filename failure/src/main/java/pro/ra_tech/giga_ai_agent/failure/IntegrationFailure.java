package pro.ra_tech.giga_ai_agent.failure;

import lombok.RequiredArgsConstructor;

public class IntegrationFailure extends AbstractFailure {
    private static final String DETAIL = "Giga chat API error";
    private final Code code;

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
        GIGA_CHAT_INTEGRATION_FAILURE("GIGA_CHAT_INTEGRATION_FAILURE");

        private final String value;

        @Override
        public String toString() {
            return value;
        }
    }
}
