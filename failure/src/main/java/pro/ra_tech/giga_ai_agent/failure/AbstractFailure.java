package pro.ra_tech.giga_ai_agent.failure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractFailure<T> implements AppFailure {
    private final T code;
    @Getter
    private final String detail;
    @Getter
    private final String source;
    @Getter
    private final @Nullable Throwable cause;
    private String message = null;

    protected AbstractFailure(T code, String detail, String source, @Nullable String message) {
        this.code = code;
        this.detail = detail;
        this.source = source;
        this.message = message;
        this.cause = null;
    }

    @Override
    public String getCode() {
        return code.toString();
    }

    @Override
    public @Nullable String getMessage() {
        if (message != null) {
            return message;
        }

        if (cause == null) {
            return "Unknown error";
        }

        val details = cause.getMessage();
        return details == null ? cause.toString() : details;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
