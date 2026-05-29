# Code Review

Review checklist for the `media-platform` project.

## 1. General Review

- The change solves the requested task without unrelated refactoring.
- There are no accidental formatting changes, IDE files, build artifacts, or temporary files.
- Class, package, module, and method names are understandable without extra context.
- `README.md` or other documentation is updated if startup, API, or project structure changed.

## 2. Java and Spring

- Controllers accept and return DTOs, not JPA/entity models.
- Input DTOs are validated with Bean Validation or another explicit mechanism.
- Business logic stays in the service layer; controllers remain thin.
- Transactions are used for operations that modify state or require consistent reads.
- Errors are converted into clear HTTP responses, and internal exceptions are not exposed outside.
- Configuration is stored in properties/yaml files or environment variables; secrets are not committed.

## 3. Microservice Boundaries

- Each new module has a clear responsibility.
- One service does not directly access another service's internal tables or entities.
- Service contracts are described through APIs, messages, or explicit DTOs.
- Network calls handle failures, timeouts, and unavailable dependencies.
- Shared code does not become a hidden monolithic layer.

## 4. Media Domain

- File uploads limit file size and allowed MIME types.
- File metadata is stored separately from binary content.
- Photo/video processing does not block user requests longer than necessary.
- File names, paths, and external identifiers cannot escape the configured storage area.
- Large files are designed with streaming and memory limits in mind.

## 5. Tests

- New business logic has unit tests.
- API contracts have web/integration tests when endpoint behavior changes.
- Negative scenarios are covered: invalid input, missing entity, and state conflicts.
- Tests do not depend on local environment details unless explicitly configured.
- The minimum project check passes:

```bash
mvn test
```

## 6. Security and Publishing

- The diff contains no `.env` files, tokens, passwords, private keys, or production configs.
- `.gitignore` covers build artifacts, IDE files, logs, and local env files.
- Errors and logs do not expose secrets.
- CORS, authorization, and media file access are not opened wider than required by the task.

## 7. Review Outcome

Before approval, verify that:

- the code compiles;
- tests pass;
- behavior matches the requested task;
- documentation does not contradict the actual repository state;
- risks and trade-offs are clearly described in the review comment.
