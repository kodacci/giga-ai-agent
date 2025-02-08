package pro.ra_tech.giga_ai_agent.core.controllers;

import io.vavr.control.Either;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.net.URI;
import java.util.Optional;

public abstract class BaseController {
    private ProblemDetail toProblemDetail(AppFailure failure) {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), failure.getDetail());
        problem.setType(URI.create("auto:blank"));
        problem.setProperty("code", failure.getCode());
        problem.setProperty("source", failure.getSource());
        problem.setProperty("message", Optional.ofNullable(failure.getCause()).map(Throwable::getMessage).orElse("Unknown error"));

        return problem;
    }

    protected <T> ResponseEntity<Object> toResponse(Either<AppFailure, T> result) {
        return result.fold(
                failure -> new ResponseEntity<Object>(toProblemDetail(failure), HttpStatus.INTERNAL_SERVER_ERROR),
                data -> new ResponseEntity<Object>(data, HttpStatus.OK)
        );
    }
}
