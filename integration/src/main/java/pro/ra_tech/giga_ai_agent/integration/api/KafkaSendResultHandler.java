package pro.ra_tech.giga_ai_agent.integration.api;

public interface KafkaSendResultHandler {
    void handleSuccess();
    void handleError(Throwable cause);
}
