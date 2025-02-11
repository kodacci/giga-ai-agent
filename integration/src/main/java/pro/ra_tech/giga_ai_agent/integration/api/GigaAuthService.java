package pro.ra_tech.giga_ai_agent.integration.api;

import io.vavr.control.Either;
import pro.ra_tech.giga_ai_agent.failure.AppFailure;

public interface GigaAuthService {
    String getClientId();
    Either<AppFailure, String> getAuthHeader();
}
