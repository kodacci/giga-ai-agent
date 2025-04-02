package pro.ra_tech.giga_ai_agent.database.repos.api;

import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceData;

public interface SourceRepository extends WritableRepository<CreateSourceData, SourceData> {
}
