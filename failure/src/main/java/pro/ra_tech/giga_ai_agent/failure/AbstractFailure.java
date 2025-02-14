package pro.ra_tech.giga_ai_agent.failure;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public abstract class AbstractFailure implements AppFailure {
    private final String source;
    private final @Nullable Throwable cause;
    private String message = null;

    public AbstractFailure(String source, @Nullable String message) {
        this.source = source;
        this.message = message;
        cause = null;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public @Nullable Throwable getCause() {
        return cause;
    }

    @Override
    public @Nullable String getMessage() {
        if (message != null) {
            return message;
        }

        if (cause == null) {
            return "Unknown error";
        }

        val message = cause.getMessage();
        return message == null ? cause.toString() : message;
    }
}
