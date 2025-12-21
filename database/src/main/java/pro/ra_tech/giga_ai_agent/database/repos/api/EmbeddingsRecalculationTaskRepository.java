package pro.ra_tech.giga_ai_agent.database.repos.api;

import pro.ra_tech.giga_ai_agent.database.repos.model.CreateRecalculationTaskData;
import pro.ra_tech.giga_ai_agent.database.repos.model.RecalculationTaskData;

public interface EmbeddingsRecalculationTaskRepository extends WritableRepository<CreateRecalculationTaskData, Long>, ReadableRepository<RecalculationTaskData> {
}
