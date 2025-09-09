package pro.ra_tech.giga_ai_agent.core.controllers;

import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
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
        return result.fold(
                failure -> {
                    log.error("Request error:", failure.getCause());
                    return new ResponseEntity<>(
                            toProblemDetail(failure),
                            MultiValueMap.fromSingleValue(Map.of("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                },
                data -> new ResponseEntity<>(data, HttpStatus.OK)
        );
    }
}
