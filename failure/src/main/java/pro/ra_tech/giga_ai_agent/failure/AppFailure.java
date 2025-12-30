package pro.ra_tech.giga_ai_agent.failure;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

public interface AppFailure {
    String getCode();
    String getDetail();
    String getSource();
    @Nullable Throwable getCause();
    @Nullable String getMessage();
    HttpStatus getHttpStatus();
}
