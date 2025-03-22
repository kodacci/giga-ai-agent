package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

import java.util.List;

public interface LlmTextProcessorService {
    Either<AppFailure, List<String>> splitText(String text);
}
