package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface ListableRepository<T> {
    Either<AppFailure, List<T>> list(long offset, int limit);
}
