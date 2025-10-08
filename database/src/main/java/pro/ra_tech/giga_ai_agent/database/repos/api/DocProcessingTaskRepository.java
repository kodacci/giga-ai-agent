package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateDocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.DocProcessingTaskStatus;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface DocProcessingTaskRepository extends WritableRepository<CreateDocProcessingTaskData, Long> {
    Either<AppFailure, DocProcessingTaskData> findById(long id);
    Either<AppFailure, Long> create(CreateDocProcessingTaskData data);
    Either<AppFailure, Integer> updateTaskStatus(long id, DocProcessingTaskStatus status);
    Either<AppFailure, Integer> updateTaskProgress(long id, int processedChunks);
    Either<AppFailure, Integer> updateTaskChunksCount(long id, int chunksCount);
}
