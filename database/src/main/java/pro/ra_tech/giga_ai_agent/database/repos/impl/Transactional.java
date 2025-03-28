package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

@Service
public class Transactional {
    private final TransactionTemplate trx;

    public Transactional(PlatformTransactionManager manager) {
        trx = new TransactionTemplate(manager);
    }

    private AppFailure toFailure(Throwable cause) {
        return new DatabaseFailure(DatabaseFailure.Code.TRANSACTION_FAILURE, getClass().getName(), cause);
    }

    public <R> Either<AppFailure, R> execute(TransactionCallback<Either<AppFailure, R>> callback) {
        return Try.of(() -> trx.execute(callback))
                .getOrElseGet(throwable -> Either.left(toFailure(throwable)));
    }
}
