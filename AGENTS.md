# AGENTS.md

Instructions for agents and assistants working in this repository.

## Project Context

This is a Java/Spring microservice project named `media-platform`. It is intended to become a media platform for photo and video content: uploading, storing, processing, and serving media files.

Current repository state: a root Maven parent project without connected modules. Do not claim that APIs, services, Docker Compose, databases, or authentication are already implemented unless the corresponding files exist in the repository.

## General Rules

- Keep responses short and practical.
- Before changing code or documentation, briefly explain the plan.
- Make minimal changes and avoid unrelated refactoring.
- If a task is ambiguous and may affect architecture, ask a question first.
- Do not delete or revert user changes unless explicitly asked.
- Do not add fake features, badges, startup instructions, or production-readiness claims.
- After changes, list the modified files and the checks that should be run.

## Java/Spring Work

- Keep DTOs separate from entities.
- Validate input data at API boundaries.
- Consider transactions for state-changing operations.
- Handle errors explicitly and predictably without leaking internal details.
- Do not mix business logic into controllers.
- Add tests for new logic according to the risk of the change.
- Avoid direct coupling between services across microservice boundaries unless there is a clear reason.

## Architecture Guidelines

The planned platform may include services for media uploads, metadata, file processing, API Gateway, and authentication. The exact service set should be decided as the project evolves.

When adding a new service:

- add it as a Maven module in the root `pom.xml`;
- describe the service purpose in `README.md`;
- document its port, dependencies, and environment variables;
- add local startup instructions;
- do not store secrets in the repository.

## ExecPlans

When writing complex features or significant refactors, use an ExecPlan, as described in `PLANS.md`, from design to implementation.

## Code review

When reviewing code, follow the rules in `code_review.md`.

## Checks

Minimum check for the current project state:

```bash
mvn test
```

If services are added, also run tests for the affected module and verify that the application starts.

## Documentation

- `README.md` describes the project for external readers.
- `PLANS.md` defines the format for detailed implementation plans.
- `code_review.md` contains the review checklist.
- New documentation must stay honest about the current repository state.
