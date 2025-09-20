package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateDocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface DocProcessingTaskRepository {
    Either<AppFailure, Long> createTask(CreateDocProcessingTaskData data);
    Either<AppFailure, Long> updateTaskStatus(long id, DocProcessingTaskStatus status);
}
