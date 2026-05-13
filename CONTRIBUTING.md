# Contributing to ZeroTrust Auth Platform

Thank you for taking the time to contribute. This document defines the standards and workflow for contributing to this project — read it fully before opening a pull request.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Prerequisites](#2-prerequisites)
3. [Getting Started](#3-getting-started)
4. [Branch Strategy](#4-branch-strategy)
5. [Commit Convention](#5-commit-convention)
6. [Code Standards](#6-code-standards)
7. [Testing Requirements](#7-testing-requirements)
8. [Pull Request Process](#8-pull-request-process)
9. [Security Guidelines](#9-security-guidelines)
10. [Reporting Issues](#10-reporting-issues)

---

## 1. Project Overview

ZeroTrust Auth Platform is an enterprise-grade Identity and Access Management (IAM) system built as a portfolio project. It implements real Zero Trust architecture across 5 microservices.

**Monorepo structure:**

```
zero-trust-auth/
├── services/
│   ├── auth-service/          # Java 21 + Spring Boot 4 — Authentication
│   ├── authz-service/         # Java 21 + Spring Boot 4 — Authorization
│   ├── audit-log-service/     # Java 21 + Spring Boot 4 — Audit Log
│   ├── ml-policy-engine/      # Python 3.12 + FastAPI — ML/Policy
│   └── api-gateway/           # Java 21 + Spring Cloud Gateway
├── infra/                     # Prometheus, Grafana, DB init scripts
├── config/                    # Checkstyle rules
├── docs/                      # Architecture, threat model, sprint docs
└── scripts/                   # Utility scripts
```

---

## 2. Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| Java (JDK) | 21 | Java services |
| Python | 3.12 | ml-policy-engine |
| Docker + Docker Compose | Latest stable | Full infrastructure |
| Git | Any modern version | Version control |

> You do **not** need to install Gradle separately — the Gradle Wrapper (`./gradlew`) is included in the repository.

---

## 3. Getting Started

### Clone and set up

```bash
git clone https://github.com/zhunio2003/zero-trust-auth.git
cd zero-trust-auth
cp .env.example .env
# Edit .env with your local values
```

### Start the infrastructure

```bash
docker compose up -d
```

All 14 containers should reach `healthy` status. Verify with:

```bash
docker compose ps
```

### Build a Java service

```bash
./gradlew :services:auth-service:compileJava
```

### Run tests

```bash
# Java (all services)
./gradlew test

# Specific service
./gradlew :services:auth-service:test

# Python
cd services/ml-policy-engine
pip install -r requirements.txt
pytest
```

### Run linting

```bash
# Java — Checkstyle (Google Style)
./gradlew checkstyleMain checkstyleTest

# Python — Flake8
cd services/ml-policy-engine
flake8 .
```

---

## 4. Branch Strategy

```
main
└── feature/<scope>/<short-description>
└── fix/<scope>/<short-description>
└── chore/<short-description>
└── docs/<short-description>
```

**Examples:**

```
feature/auth/totp-enrollment
feature/auth/webauthn-registration
fix/auth/invalid-credentials-handler
chore/update-checkstyle-rules
docs/add-sprint3-planning
```

**Rules:**

- `main` is always in a deployable state. Never commit directly to `main`.
- All work happens in feature branches. Open a pull request to merge.
- Branch names are lowercase with hyphens. No spaces, no uppercase.
- Keep branches short-lived — one story or task per branch.

---

## 5. Commit Convention

This project follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

### Format

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

### Types

| Type | When to use |
|------|-------------|
| `feat` | New feature or behavior |
| `fix` | Bug fix |
| `test` | Adding or modifying tests |
| `refactor` | Code change that is neither a fix nor a feature |
| `docs` | Documentation only (READMEs, architecture docs, guides) |
| `chore` | Maintenance — build scripts, configs, CHANGELOG, dependency updates |
| `ci` | Changes to CI/CD pipeline (`ci.yml`, workflows) |
| `style` | Formatting, whitespace — no logic change |
| `perf` | Performance improvement |
| `revert` | Reverts a previous commit |

### Scopes

Use the service or layer being changed:

`auth`, `authz`, `audit`, `ml`, `gateway`, `infra`, `ci`, `docs`, `config`

### Examples

```bash
feat(auth): add TOTP enrollment endpoint
fix(auth): throw InvalidCredentialsException for unknown email
test(auth): add unit test for duplicate email registration
chore: add CHANGELOG following Keep a Changelog format
ci: add conditional docker build using git diff
docs: add sprint 2 planning document
refactor(auth): extract MFA session logic into MfaSessionService
```

### Rules

- Use the **imperative mood** in the description: "add", "fix", "update" — not "added", "fixes", "updating".
- Description is lowercase, no period at the end.
- Keep the first line under 72 characters.
- Commit at logical checkpoints — after the code compiles and tests pass, not at end of day.
- Run `./gradlew :services:<service>:compileJava` before committing Java changes.

---

## 6. Code Standards

### Java

- **Style:** Google Java Style, enforced by Checkstyle (`config/checkstyle/checkstyle.xml`).
- **Formatting:** 2-space indentation, 100-character line limit.
- **Naming:** `camelCase` for methods and variables, `PascalCase` for classes, `UPPER_SNAKE_CASE` for constants.
- **No hardcoded secrets:** all credentials and sensitive config via environment variables, never in source code.
- **Field naming:** use domain-accurate names. Example: `passwordHash`, not `password`, for a BCrypt digest.
- **DTOs:** use `@Builder` for response DTOs. Use Bean Validation annotations (`@NotBlank`, `@Email`, `@Size`) on request DTOs.
- **Exceptions:** define custom exceptions for domain errors. Map them to HTTP status codes in `GlobalExceptionHandler`.

### Python

- **Style:** PEP 8, enforced by Flake8.
- **Max line length:** 120 characters (configured in `.flake8`).
- **Type hints:** required on all function signatures.

### General

- No commented-out code in committed files.
- No `TODO` comments without an associated issue number: `// TODO(#42): implement rate limiting`.
- No `System.out.println` or `print()` for logging — use SLF4J (`log.info`, `log.error`) in Java and the `logging` module in Python.

---

## 7. Testing Requirements

Every story must have tests before it can be considered done (see `docs/DEFINITION_OF_DONE_ZEROTRUST.md`).

### Java — Unit Tests

- Framework: JUnit 5 + Mockito (via `spring-boot-starter-test`).
- Location: `src/test/java/` mirroring the production package structure.
- Test profile: `application-test.yml` uses H2 in-memory database — no external dependencies required to run unit tests.
- Each service class must have a corresponding test class covering: happy path, error path, and edge cases.

```java
// Naming convention
class RegistrationServiceTest {
    @Test
    void registerUser_whenEmailAlreadyExists_throwsUserAlreadyExistsException() { ... }

    @Test
    void registerUser_whenValid_savesUserWithHashedPassword() { ... }
}
```

### Python — Unit Tests

- Framework: Pytest.
- Location: `tests/` directory inside the service.

### Running the full test suite

```bash
./gradlew test                          # All Java services
./gradlew :services:auth-service:test   # Single service
```

---

## 8. Pull Request Process

1. Make sure all tests pass locally before opening a PR.
2. Make sure Checkstyle / Flake8 produce no errors.
3. Use the pull request template (`.github/pull_request_template.md`) — fill in every section.
4. Title must follow Conventional Commits format: `feat(auth): add TOTP enrollment`.
5. Link the related issue or story ID in the PR description.
6. The CI pipeline must be green before the PR can be merged.
7. Squash-merge into `main` to keep the history linear.

---

## 9. Security Guidelines

This project implements Zero Trust architecture — security is not an afterthought.

- **Never commit secrets.** No API keys, passwords, private keys, tokens, or connection strings in source code. Use `.env` (excluded from git via `.gitignore`) and reference variables in `application.yml` as `${ENV_VAR_NAME}`.
- **Enumeration defense.** Authentication endpoints must never reveal whether an email exists. Return the same error message and HTTP status regardless of whether the failure is "email not found" or "wrong password".
- **MFA is not optional.** Login flows must enforce the MFA session token step — do not add bypass paths.
- **Secrets in tests.** Test configuration (`application-test.yml`) must not contain real credentials, even for development environments.
- **Dependencies.** When adding a new dependency, verify it on [Maven Central](https://central.sonatype.com) or [PyPI](https://pypi.org). Do not add dependencies from unknown sources.

If you discover a security vulnerability, **do not open a public issue.** Contact the maintainer directly.

---

## 10. Reporting Issues

Use the GitHub Issue templates:

- **Bug report** (`.github/ISSUE_TEMPLATE/bug_report.md`) — for defects in existing behavior.
- **Feature request** (`.github/ISSUE_TEMPLATE/feature_request.md`) — for new functionality.

Before opening an issue:
- Check if a similar issue already exists.
- For bugs, include steps to reproduce, expected behavior, and actual behavior.
- Attach relevant logs or stack traces.

---

## References

- [Conventional Commits Specification](https://www.conventionalcommits.org/en/v1.0.0/)
- [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [PEP 8 — Python Style Guide](https://peps.python.org/pep-0008/)
- [Definition of Done — ZeroTrust Auth Platform](docs/DEFINITION_OF_DONE_ZEROTRUST.md)
- [Threat Model STRIDE — ZeroTrust Auth Platform](docs/THREAT-MODEL_STRIDE.md)
