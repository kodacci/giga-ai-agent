package pro.ra_tech.giga_ai_agent.database.repos.impl;

import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

public abstract class BaseRepository {
    protected abstract DatabaseFailure.Code failureCode();

    protected AppFailure toFailure(Throwable cause) {
        return new DatabaseFailure(
                failureCode(),
                getClass().getName(),
                cause
        );
    }
}
