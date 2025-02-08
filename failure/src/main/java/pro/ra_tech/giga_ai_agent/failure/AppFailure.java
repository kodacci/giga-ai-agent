package pro.ra_tech.giga_ai_agent.failure;

import org.springframework.lang.Nullable;

public interface AppFailure {
    String getCode();
    String getDetail();
    String getSource();
    @Nullable Throwable getCause();
    @Nullable String getMessage();
}
