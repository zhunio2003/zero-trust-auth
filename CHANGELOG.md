# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### In Progress
- `feat(auth): implement TOTP enrollment and verification flow (T-02.2.2)`

---

## [0.2.0] — 2026-05-12

### Sprint 2 — EP-02: Authentication Service (partial)

---

### Added — US-02.1: User Registration

- `User` entity with UUID primary key (`gen_random_uuid()`), `passwordHash`, `email`, `createdAt`, `updatedAt` fields
- `UserRepository` extending `JpaRepository<User, UUID>`
- `RegistrationRequest` DTO with Bean Validation (`@NotBlank`, `@Email`, `@Size(min=12)`)
- `RegistrationResponse` DTO
- `RegistrationService` with BCrypt password hashing via `PasswordEncoder`
- `AuthController` with `POST /api/v1/auth/register` endpoint
- `GlobalExceptionHandler` with `@RestControllerAdvice` for `MethodArgumentNotValidException` and `UserAlreadyExistsException`
- `UserAlreadyExistsException` custom exception
- Springdoc OpenAPI integration (`/swagger-ui.html`, `/v3/api-docs`)
- Unit tests: `RegistrationServiceTest` covering happy path, duplicate email, and password encoding with Mockito

### Added — US-02.2: Login with MFA (T-02.2.1)

- `LoginRequest` DTO (`email`, `password`)
- `LoginResponse` DTO with `@Builder` (`mfaSessionToken`, `mfaRequired`)
- `LoginService` implementing credential validation with `PasswordEncoder.matches()`
- MFA session token pattern: Redis key `"mfa:{UUID}"` → `userId` with 5-minute TTL via `StringRedisTemplate`
- `InvalidCredentialsException` mapped to HTTP 401 in `GlobalExceptionHandler`
- `POST /api/v1/auth/login` endpoint in `AuthController`

### Security

- Password minimum length set to 12 characters (exceeds the 8-character acceptance criteria — deliberate security decision)
- Enumeration defense: identical `InvalidCredentialsException` thrown for both unknown email and wrong password
- MFA session tokens are single-use UUIDs with short TTL, never exposing `userId` directly to the client

### Database

- `users` table: `id UUID`, `email VARCHAR(255) UNIQUE NOT NULL`, `password_hash VARCHAR(255) NOT NULL`, `created_at TIMESTAMPTZ`, `updated_at TIMESTAMPTZ`
- `totp_credentials` table: `id UUID`, `user_id UUID REFERENCES users(id) ON DELETE CASCADE`, `secret VARCHAR(255)`, `verified BOOLEAN DEFAULT FALSE`, `created_at TIMESTAMPTZ`
- `webauthn_credentials` table: `id UUID`, `user_id UUID REFERENCES users(id) ON DELETE CASCADE`, `credential_id VARCHAR(512) UNIQUE`, `public_key TEXT`, `sign_count BIGINT DEFAULT 0`, `created_at TIMESTAMPTZ`

---

## [0.1.0] — 2026-04-08

### Sprint 1 — EP-01: Infrastructure Setup

---

### Added

- Gradle monorepo initialized with 5 services: `auth-service`, `authz-service`, `audit-log-service`, `ml-policy-engine` (Python/FastAPI), `api-gateway`
- Root `build.gradle` with shared Checkstyle configuration (Google Style) applied to all Java subprojects
- `settings.gradle` declaring all subprojects under `com.mazr.zerotrust`
- `.gitignore` (multi-language: Java, Python, Node, Docker, IDE), `.editorconfig`, `.env.example`
- `README.md` with project overview, security model, tech stack, folder structure, and getting started guide
- GitHub issue templates: `bug_report.md`, `feature_request.md`, `pull_request_template.md`

### CI/CD

- GitHub Actions workflow `ci.yml` with 5 jobs in two parallel lanes:
    - `lint-java` → `test-java` → `build-docker` (Java lane)
    - `lint-python` → `test-python` → `build-docker` (Python lane)
- Conditional Docker build using `strategy.matrix` and `git diff` — only rebuilds services with changes
- JUnit and Pytest test reports uploaded as artifacts on every run (`if: always()`)
- Checkstyle `config/checkstyle/checkstyle.xml` with Google Style rules

### Infrastructure (Docker Compose)

- `docker-compose.yml` with 14 containers across 4 segmented Docker networks:

  | Network | Type | Purpose |
    |---------|------|---------|
  | `public` | External | frontend, api-gateway |
  | `services` | External | microservices, kafka |
  | `data` | **Internal** | databases, redis |
  | `monitoring` | **Internal** | prometheus, grafana |

- Infrastructure containers verified `healthy`:
    - `postgres-authn` (PostgreSQL 17)
    - `postgres-authz` (PostgreSQL 17)
    - `mongodb-auditlog` (MongoDB 8)
    - `mongodb-ml` (MongoDB 8)
    - `redis` (Redis 8)
    - `kafka` (Apache Kafka 3.9.0, KRaft mode)
    - `prometheus`
    - `grafana`

- `api-gateway`: Dockerfile, `application.yml`, `build.gradle` configured (Spring Cloud Gateway 2025.1.1)

### Fixed

- Kafka `KAFKA_LISTENERS` must specify explicit hostname, not `0.0.0.0` — KRaft rejects wildcard bind
- Prometheus healthcheck replaced `curl` with `wget --spider` (curl not available in the base image)
- MongoDB auth credentials corrected in `docker-compose.yml`
- Checkstyle 10.x: `LineLength` rule moved out of `<TreeWalker>` to root — build was failing with older config pattern
- Gradle monorepo Dockerfiles require copying all subproject `build.gradle` files at build time (not just the target service's)
- Spring Cloud 2025.0.0 incompatible with Spring Boot 4.0.x — pinned to `2025.1.1`; artifact renamed to `spring-cloud-starter-gateway-server-webmvc`

---

## [0.0.0] — 2026-03-31

### Sprint 0 — Project Foundation

### Added

- Product Vision Board
- STRIDE Threat Model (9 threats identified with mitigations)
- Detailed Architecture document (4 microservices, Zero Trust principles)
- Component Diagram
- Deployment Diagram
- Technology Stack decisions document
- Definition of Done (3 levels: Story, Sprint, Release)
- Product Backlog with 6 epics across 3 phases

---

[Unreleased]: https://github.com/zhunio2003/zero-trust-auth/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/zhunio2003/zero-trust-auth/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/zhunio2003/zero-trust-auth/compare/v0.0.0...v0.1.0
[0.0.0]: https://github.com/zhunio2003/zero-trust-auth/releases/tag/v0.0.0
