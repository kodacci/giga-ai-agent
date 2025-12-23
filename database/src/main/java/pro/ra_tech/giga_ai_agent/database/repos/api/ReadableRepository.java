package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface ReadableRepository<T> {
    Either<AppFailure, T> findById(long id);
}
