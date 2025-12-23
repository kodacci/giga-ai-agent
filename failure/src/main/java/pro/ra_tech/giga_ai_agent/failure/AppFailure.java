package pro.ra_tech.giga_ai_agent.failure;

import org.jspecify.annotations.Nullable;

public interface AppFailure {
    String getCode();
    String getDetail();
    String getSource();
    @Nullable Throwable getCause();
    @Nullable String getMessage();
}
