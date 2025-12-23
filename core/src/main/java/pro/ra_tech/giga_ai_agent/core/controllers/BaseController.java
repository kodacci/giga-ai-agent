package pro.ra_tech.giga_ai_agent.core.controllers;

import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.*;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.net.URI;
import java.util.Optional;

@Slf4j
public abstract class BaseController {
    private ProblemDetail toProblemDetail(AppFailure failure) {
        val problem = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), failure.getDetail());
        problem.setType(URI.create("auto:blank"));
        problem.setProperty("code", failure.getCode());
        problem.setProperty("source", failure.getSource());
        problem.setProperty("message", failure.getMessage());
        problem.setProperty(
                "trace",
                Optional.ofNullable(failure.getCause())
                        .map(Throwable::getStackTrace)
                        .orElse(null)
        );

        return problem;
    }

    protected <T> ResponseEntity<Object> toResponse(Either<AppFailure, T> result) {
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return result.fold(
                failure -> {
                    log.error("Request error:", failure.getCause());
                    return new ResponseEntity<>(
                            toProblemDetail(failure),
                            headers,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                },
                data -> new ResponseEntity<>(data, HttpStatus.OK)
        );
    }
}
