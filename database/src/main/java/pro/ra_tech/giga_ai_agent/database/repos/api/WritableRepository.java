package pro.ra_tech.giga_ai_agent.database.repos.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface WritableRepository<T, R> {
    Either<AppFailure, R> create(T data);
}
