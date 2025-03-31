package pro.ra_tech.giga_ai_agent.database.repos.impl;

import io.vavr.control.Either;
import io.vavr.control.Try;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;
import pro.ra_tech.giga_ai_agent.failure.DatabaseFailure;

import java.util.function.Function;

@Service
public class Transactional {
    public static class TransactionalException extends RuntimeException {
        public TransactionalException(AppFailure failure) {
            super(
                    String.format("Transaction terminated with application failure: %s", failure.getMessage()),
                    failure.getCause()
            );
        }
    }

    private final TransactionTemplate trx;

    public Transactional(PlatformTransactionManager manager) {
        trx = new TransactionTemplate(manager);
    }

    private AppFailure toFailure(Throwable cause) {
        return new DatabaseFailure(DatabaseFailure.Code.TRANSACTION_FAILURE, getClass().getName(), cause);
    }

    public <R> Either<AppFailure, R> execute(Function<TransactionStatus, Either<AppFailure, R>> callback) {
        return Try.of(() -> trx.execute(
                state -> callback.apply(state)
                        .fold(
                                failure -> { throw new TransactionalException(failure); },
                                Either::<AppFailure, R>right
                        )
        ))
                .getOrElseGet(throwable -> Either.left(toFailure(throwable)));
    }
}
