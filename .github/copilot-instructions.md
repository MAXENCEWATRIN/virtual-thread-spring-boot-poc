## Purpose
Help AI agents be productive in this repository by describing the project shape, build/run/test workflows, and project-specific conventions.

## Quick facts
- Language: Java (Spring Boot)
- Build: Maven (wrapper present: `mvnw`, `mvnw.cmd`)
- Main class: `src/main/java/com/example/demo/VirtualThreadApplication.java`
- Packaging: standard Spring Boot jar (artifactId `demo`, version `0.0.1-SNAPSHOT`)
- Java version: declared `java.version=25` in `pom.xml` — prefer JDK 25 for local runs

## How to build, test, run (Windows-focused examples)
- Build (with wrapper):
  - `./mvnw -DskipTests package` (UNIX)
  - `.\mvnw.cmd -DskipTests package` (PowerShell / Windows)
- Run via Maven (dev): `.\mvnw.cmd spring-boot:run`
- Run packaged jar (after build): `java -jar target\demo-0.0.1-SNAPSHOT.jar`
- Run tests: `.\mvnw.cmd test`

Note: this repo lives under OneDrive in the current workspace which can occasionally cause file-locking/sync issues on Windows; if CI or local runs behave oddly, try a non-synced local folder.

## What to look at (key files)
- `pom.xml` — dependency list, Spring Boot parent (3.5.7), Java version property, Spring Boot Maven plugin
- `src/main/java/com/example/demo/VirtualThreadApplication.java` — Spring Boot entrypoint
- `src/main/resources/application.yaml` — app name is `virtual-thread`; no DB config stored here by default
- `src/test/java/com/example/demo/VirtualThreadApplicationTests.java` — example integration test using `@SpringBootTest`

## Project-specific patterns & conventions
- Minimal demo project: keep changes small and self-contained under `com.example.demo` package.
- Tests use JUnit Jupiter + Spring Boot Test; prefer `@SpringBootTest` for end-to-end context loads and plain unit tests for small logic.
- Runtime-postgres dependency is declared (postgresql runtime scope) — the project may be wired for DB demos; if you add DB-related configuration, keep credentials out of repo and use environment variables or profiles.
- Java version is intentionally high (25). Do not down-level unless you also update `pom.xml` and CI.

## Integration points & external dependencies
- PostgreSQL is declared as a runtime dependency in `pom.xml`. There is no connection configured in `application.yaml` yet; assume external DB configuration is required for DB demos.
- No other external services are declared in the repo. If you add integrations, document required env vars in `application.yaml` and `README.md`.

## Guidance for AI changes
- Make small, focused PRs: add a single controller or service class in `src/main/java/com/example/demo` and matching tests under `src/test/java/com/example/demo`.
- Preserve package structure and artifact coordinates in `pom.xml` unless the change is explicitly about packaging or groupId/artifactId.
- Do not modify files under `target/` — they are build outputs.
- If you need to change Java target, update `pom.xml` `java.version` property and ensure CI/JDK alignment.

## Examples to reference when making edits
- To add a REST endpoint, create `src/main/java/com/example/demo/HelloController.java` with `@RestController` and a simple `@GetMapping("/hello")` returning text. Add a test under `src/test/java/com/example/demo/HelloControllerTest.java` using MockMvc or `@SpringBootTest`.
- To add DB wiring, update `application.yaml` with profile-safe placeholders and document required env vars in `README.md`.

## If something is unclear
- Ask for which JDK is available in CI or whether it's acceptable to require JDK 25 locally.
- Confirm if demonstration of virtual threads is expected in code changes; the repo name suggests a proof-of-concept but no virtual-thread APIs are present in the current files — if needed, request guidance on which scenarios to demonstrate (e.g., servlet thread per request vs virtual threads in executor services).

---
If you'd like, I can now create a small example controller + test that demonstrates virtual-thread-friendly async behavior and update `README.md` with run steps.
