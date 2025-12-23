package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.database.repos.model.CreateSourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceData;
import pro.ra_tech.giga_ai_agent.database.repos.model.SourceWithTagsDto;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface SourceRepository extends WritableRepository<CreateSourceData, SourceData>, ListableRepository<SourceWithTagsDto> {
    Either<AppFailure, List<String>> getNames(List<Long> ids);
}
