package pro.ra_tech.giga_ai_agent.core.controllers.document_enqueue.dto;

import lombok.RequiredArgsConstructor;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;

@RequiredArgsConstructor
public enum DocumentProcessingStatus {
    IDLE("IDLE"),
    STARTED("STARTED"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static DocumentProcessingStatus of(DocProcessingTaskStatus status) {
        return switch (status) {
            case STARTED -> STARTED;
            case SUCCESS -> SUCCESS;
            case ERROR -> ERROR;
            default -> IDLE;
        };
    }
}
