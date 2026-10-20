# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & test

Multi-module Maven project (Java 25, Spring Boot 4, Lombok). Always use the wrapper.

```bash
./mvnw -DskipTests clean package          # build (core produces core/target/giga-ai-agent-core.jar)
./mvnw verify                             # full build + all tests + aggregated JaCoCo report
./mvnw -pl core -am verify -Dit.test=SourcesApiIT \
  -Dsurefire.failIfNoSpecifiedTests=false -Dfailsafe.failIfNoSpecifiedTests=false   # single IT class
./mvnw -pl database -am verify -Dit.test='EmbeddingRepositoryIT#methodName' ...      # single IT method
```

- Tests are integration tests named `*IT` and run by **failsafe** in `verify` (not `test`). They need a running Docker daemon: Testcontainers starts PostgreSQL (with pgvector) and Kafka; external HTTP APIs are stubbed with MockServer (fixtures in `core/src/test/resources/mockserver/`). Tests run with the `test` Spring profile (`application-test.yaml`).
- Coverage report: `code-coverage/target/site/jacoco-aggregate/jacoco.xml` (module `code-coverage` only aggregates).
- CI (`Jenkinsfile`): `package -DskipTests` → `verify` → Sonar (main only) → Nexus deploy with version `<version>-<branch-leaf>-SNAPSHOT` → Docker images from `distrib/docker/{core,db-migrate}` → triggers the CD job. K8s/Istio manifests live in `distrib/templates`.

## Module layout and dependency direction

- `integration` → `failure`
- `database` → `failure`, `db-migrate`
- `domain` → `integration`, `database`, `failure`
- `core` → all of the above

- **failure** — `AppFailure` interface + `AbstractFailure<CodeEnum>` subclasses (`DatabaseFailure`, `IntegrationFailure`, `DocumentProcessingFailure`, `RecalculationFailure`). Each failure carries a code enum, source, cause and HTTP status.
- **integration** — clients for external systems: GigaChat (Sber), YandexGPT, Cloud.ru (via `openai-java` `OpenAIClient`, OpenAI-compatible API), HFS file server, Telegram Bot API, an LLM text-processor (text splitting), and Kafka. REST clients are Retrofit + Failsafe retries, extending `BaseRestService`, with Micrometer counters/timers. Each external system has a `config/<name>/` package with a `*Config` and `*Props` (`@ConfigurationProperties` under `app.<name>`).
- **database** — plain JDBC repositories (no JPA) in `repos/api` + `repos/impl`, pgvector for embeddings. Transactions go through the `Transactional` helper (`trx.execute(status -> Either...)`), which rolls back when the callback returns `Left`.
- **db-migrate** — Liquibase YAML changelogs in `src/main/resources/db/changelogs/changelog-1.0/`, named `YYYY_MM_DD__HH_mm_ss__description.yaml` and registered from `db/changelog-root.yaml`. Shipped as its own Docker image/K8s Job and also applied by Spring Liquibase in tests.
- **domain** — business logic: document ingestion (PDF via PDFBox, TXT), embedding creation, embedding recalculation, Telegram bot (listener + handler threads fed by a `BlockingQueue`), Kafka task handlers, Giga balance gauges.
- **core** — Spring Boot entrypoint (`CoreApplication`), controllers, thin API services. `MainConfiguration` `@Import`s every module's config class — **a new module/integration config must be added there**. Actuator/Prometheus on port 8081.

## Key conventions

- **Errors are values, not exceptions.** Service/repository/client methods return `io.vavr.control.Either<AppFailure, T>`; compose with `map`/`flatMap`, wrap throwing code with `Try`. Controllers extend `BaseController` and return `toResponse(either)`, which turns a `Left` into an RFC 7807 `ProblemDetail` using the failure's HTTP status.
- Beans in `domain`/`integration` are mostly wired explicitly with `@Bean` methods in the module's `*Config` classes (constructor injection, `@RequiredArgsConstructor`), not by classpath scanning. Use Lombok `val`, records for DTOs/props, and `org.jspecify` `@Nullable`.
- Controllers are split into an interface `*Api` (OpenAPI/springdoc annotations) and an implementing `*Controller`; API versions live in `v1`/`v2` packages.
- **Multiple AI providers side by side.** GigaChat and Cloud.ru each have their own service packages (`core/services/{api,impl}/{giga,cloud_ru}`) and their own domain implementations sharing abstract bases: `BaseEmbeddingService` → `GigaEmbeddingService` / `QwenEmbeddingService`, `BaseEmbeddingsRecalculationService` → `GigaEmbeddingRecalculationService` / `QwenEmbeddingsRecalculationService`. Same-typed beans are distinguished by name (e.g. `gigaEmbeddingService`, `cloudRuEmbeddingService`). Stored embeddings record which `EmbeddingModel` produced them.

## Async processing (Kafka)

Long-running work is queued, tracked in a DB task table, and processed by `KafkaTaskListener` (single listener on several topics, dispatching on payload type):
- Document enqueue: upload stored on HFS → `DocumentProcessingTask` → split into chunks (`ChunkProcessingTask`) → embeddings; status in `document_processing_tasks`.
- Embedding recalculation: `EmbeddingRecalculationTask` re-embeds stored chunks with another model; status in `embeddings_recalculation_tasks`.

Topic names come from `app.kafka.*`. Follow the same "create task row → send Kafka message → handler updates status" pattern for new async work.
