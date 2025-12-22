package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateRecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskStatus;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface EmbeddingsRecalculationTaskRepository extends WritableRepository<CreateRecalculationTaskData, Long>,
        ReadableRepository<RecalculationTaskData> {
    Either<AppFailure, Void> updateStatus(long id, RecalculationTaskStatus status);
    Either<AppFailure, Integer> incrementTaskProgress(long id);
}
