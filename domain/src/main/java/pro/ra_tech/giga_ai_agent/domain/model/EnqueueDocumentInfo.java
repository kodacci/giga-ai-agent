package pro.ra_tech.giga_ai_agent.domain.model;

public record EnqueueDocumentInfo(
        long taskId,
        String hfsFileName
) {
}
