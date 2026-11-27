<p align="center"><strong>English</strong> · <a href="README.es.md">Español</a></p>

<div align="center">
    <h1>ZERO  TRUST — Enterprise Authentication & Authorization Platform</h1>
<img src="docs/brand/logo-zero.jpeg" alt="logo zerotrust" width="900">
<br/><br/>

<img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java"/>
<img src="https://img.shields.io/badge/Spring%20Boot-4.0.4-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/Spring%20Cloud-2025.1.1-6DB33F?logo=spring&logoColor=white" alt="Spring Cloud"/>
<img src="https://img.shields.io/badge/Spring%20Security-planned-6DB33F?logo=springsecurity&logoColor=white" alt="Spring Security"/>
<br/>

<img src="https://img.shields.io/badge/Python-3.x-3776AB?logo=python&logoColor=white" alt="Python"/>
<img src="https://img.shields.io/badge/FastAPI-latest-009688?logo=fastapi&logoColor=white" alt="FastAPI"/>
<img src="https://img.shields.io/badge/scikit--learn-planned-F7931E?logo=scikitlearn&logoColor=white" alt="scikit-learn"/>
<br/>

<img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
<img src="https://img.shields.io/badge/MongoDB-8-47A248?logo=mongodb&logoColor=white" alt="MongoDB"/>
<img src="https://img.shields.io/badge/Redis-8-DC382D?logo=redis&logoColor=white" alt="Redis"/>
<br/>

<img src="https://img.shields.io/badge/Apache%20Kafka-3.9.0%20KRaft-231F20?logo=apachekafka&logoColor=white" alt="Apache Kafka"/>
<br/>

<img src="https://img.shields.io/badge/Prometheus-latest-E6522C?logo=prometheus&logoColor=white" alt="Prometheus"/>
<img src="https://img.shields.io/badge/Grafana-latest-F46800?logo=grafana&logoColor=white" alt="Grafana"/>
<img src="https://img.shields.io/badge/OpenTelemetry-planned-7B61FF?logo=opentelemetry&logoColor=white" alt="OpenTelemetry"/>
<br/>

<img src="https://img.shields.io/badge/Docker%20Compose-latest-2496ED?logo=docker&logoColor=white" alt="Docker Compose"/>
<img src="https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?logo=githubactions&logoColor=white" alt="GitHub Actions"/>
<img src="https://img.shields.io/badge/Gradle-9.4.1-02303A?logo=gradle&logoColor=white" alt="Gradle"/>
<br/>

<img src="https://img.shields.io/badge/Checkstyle-Google%20Style-FF6F00" alt="Checkstyle"/>
<img src="https://img.shields.io/badge/Flake8-Python%20Linter-3776AB" alt="Flake8"/>
<img src="https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white" alt="JUnit"/>
<img src="https://img.shields.io/badge/Pytest-latest-0A9EDC?logo=pytest&logoColor=white" alt="Pytest"/>
<img src="https://img.shields.io/badge/Mockito-latest-78A641" alt="Mockito"/>
<br/>

<img src="https://img.shields.io/badge/JWT-RS256-000000?logo=jsonwebtokens&logoColor=white" alt="JWT RS256"/>
<img src="https://img.shields.io/badge/TOTP-RFC%206238-2C3E50" alt="TOTP"/>
<img src="https://img.shields.io/badge/WebAuthn-Passkeys-4285F4?logo=webauthn&logoColor=white" alt="WebAuthn"/>
<img src="https://img.shields.io/badge/ABAC-Authorization-8E44AD" alt="ABAC"/>
<img src="https://img.shields.io/badge/STRIDE-Threat%20Model-C0392B" alt="STRIDE"/>
<br/>

<img src="https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger&logoColor=black" alt="Swagger"/>
<img src="https://img.shields.io/badge/Nginx-planned-009639?logo=nginx&logoColor=white" alt="Nginx"/>
<br/><br/>
</div>
> Enterprise-grade authentication & authorization platform built from scratch with microservices architecture. Implements real Zero Trust: every request is verified continuously based on identity, context, and behavior.

---

## What is this?

ZeroTrust Auth is an enterprise-grade authentication and authorization API, built from scratch on a microservices architecture. It implements the real Zero Trust model: no request is trusted by default. Every session is verified continuously based on identity, context and behavior.

The platform provides multi-layered security mechanisms, including MFA, JWT lifecycle management and distributed rate limiting. A machine-learning anomaly detection engine monitors every authentication and authorization event in real time, computing a per-request trust score and alerting administrators when suspicious patterns emerge. Full observability is built in from day one: metrics, historical data and a real-time admin dashboard give complete visibility into the system at all times.

---
 
## Architecture Overview

> **Deep dive:** [Detailed Architecture](docs/architecture/DETAILED_ARCHITECTURE.md) · [Component Diagram](docs/diagrams/COMPONENT_DIAGRAM.mermaid)

---
 
## Key Features

- **Multi-Factor Authentication (MFA)** — supports TOTP (time-based one-time codes) and WebAuthn/Passkeys bound to the legitimate domain
- **JWT Lifecycle Management** — RS256-signed tokens, refresh token rotation and immediate revocation through a Redis blacklist
- **Distributed Rate Limiting** — Token Bucket algorithm implemented from scratch, with counters shared across instances
- **ABAC Authorization** — attribute-based authorization that evaluates role, department, time of day, resource sensitivity and the ML trust score
- **ML Anomaly Detection** — real-time trust score based on IP, behavior patterns, request velocity and user history
- **Immutable Audit Logs** — hash chaining makes any tampering with an event detectable and gives every record forensic value
- **Real-time Observability** — Prometheus metrics, Grafana dashboards and automatic alerts when anomalous patterns are detected
- **Admin Dashboard** — full visibility into active sessions, audit logs, ABAC policies and real-time threat monitoring

---

## Security Model

This platform implements a real Zero Trust model, built from scratch: no request is trusted by default, and every access is verified continuously.
Security is enforced at multiple levels:

- **Identity verification** — mandatory MFA with TOTP and WebAuthn. No second factor, no access.
- **Token security** — RS256-signed JWTs. Tokens using the `none` algorithm or carrying invalid signatures are explicitly rejected. Immediate revocation in Redis on logout or when a token reuse attack is detected.
- **Authorization** — ABAC policies evaluated on every request, combining user attributes, context and the ML trust score. Deny by default for any case not covered.
- **Rate limiting** — a distributed Token Bucket algorithm limits requests per IP. It is the first filter, before any credential check.
- **Audit integrity** — hash chaining on every audit event. Any modification breaks the chain and is detectable.
- **Error handling** — generic error messages to the client. No response exposes internal structure, table names or stack traces.
- **Transport** — strict CORS configured at the API Gateway. WebAuthn binds authentication to the legitimate domain.

> **Deep dive:** [Threat Model — STRIDE](docs/security/THREAT-MODEL_STRIDE_ZEROTRUST.md)
 
---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21 + Spring Boot 4.0.4 |
| ML / Policy Engine | Python 3.12 + FastAPI |
| Messaging | Apache Kafka 3.9 (KRaft) |
| Relational DB | PostgreSQL 16 |
| Document DB | MongoDB 7 |
| Cache / Sessions | Redis 7 |
| Observability | Prometheus + Grafana + OpenTelemetry |
| Containers | Docker + Docker Compose |
| CI/CD | GitHub Actions |

> **Deep dive:** [Technology Stack](docs/stack/TECHNOLOGY_STACK_ZEROTRUST.md)
 
---
 
## Project Structure

<details>
<summary><strong>Repository layout</strong> (click to expand)</summary>

```
zero-trust-auth/
│
├── services/                        # Backend microservices
│   ├── auth-service/                # Authentication Service — Java + Spring Boot
│   ├── authz-service/               # Authorization Service — Java + Spring Boot
│   ├── audit-log-service/           # Audit Log Service — Java + Spring Boot
│   ├── ml-policy-engine/            # ML + Policy Engine — Python + FastAPI
│   └── api-gateway/                 # API Gateway — Java + Spring Boot
│
├── frontend/                        # Admin Dashboard — React
│
├── infra/                           # Infrastructure configuration
│   ├── kafka/                       # Kafka broker configuration
│   ├── prometheus/                  # Prometheus scrape targets
│   ├── grafana/provisioning/        # Grafana datasources and dashboards
│   └── db/                          # Database init scripts
│       ├── postgres-authn/          # AuthN schema (users, credentials, MFA)
│       ├── postgres-authz/          # AuthZ schema (ABAC policies)
│       ├── mongodb-audit/           # Audit Log collections and indexes
│       └── mongodb-ml/             # ML behavior data collections
│
├── scripts/                         # Utility scripts
│   ├── generate-keys.sh             # RSA key pair generation for JWT signing
│   ├── seed-data.sh                 # Test data seeding
│   └── verify-audit-chain.sh        # Audit log hash chain integrity check
│
├── docs/                            # Project documentation
│   ├── adr/                         # Architecture Decision Records
│   ├── architecture/                # Detailed Architecture, Component & Deployment Diagrams
│   ├── brand/                       # Logo and visual assets
│   ├── diagrams/                    # Mermaid source files
│   ├── images/                      # Exported diagrams and screenshots
│   ├── product/                     # Vision Board, DoD, Backlog, Sprint Planning
│   ├── security/                    # Threat Model STRIDE
│   └── stack/                       # Technology Stack decisions
│
├── .github/                         # GitHub Actions CI/CD and templates
├── docker-compose.yml               # Container orchestration (4 segmented networks)
├── build.gradle                     # Shared Gradle config for Java services
├── settings.gradle                  # Gradle monorepo subproject definitions
└── README.md
```

</details>
 
Each microservice owns its own database (database-per-service principle). No service accesses another service's storage directly. Communication between services follows the synchronous flow `Gateway → AuthN → AuthZ → Policy Engine`, with asynchronous event publishing to Kafka for audit logging and ML training.
 
---
 
## Getting Started

### Prerequisites

- Docker + Docker Compose
- Git

### Installation

1. Clone the repository
```bash
   git clone https://github.com/zhunio2003/zero-trust-auth.git
   cd zero-trust-auth
```

2. Configure environment variables
```bash
   cp .env.example .env
   # Edit .env with your values
```

3. Generate RSA keys for JWT signing
```bash
   chmod +x scripts/generate-keys.sh
   ./scripts/generate-keys.sh
```

4. Start all services
```bash
   docker compose up
```

5. Verify the system is running
```bash
   curl http://localhost:8080/health
```
### Access points

| Service | URL |
|---------|-----|
| API Gateway | http://localhost:8080 |
| Admin Dashboard | http://localhost:80 |
| Grafana | http://localhost:3000 |

---
 
## API Reference

Full API documentation is available via Swagger UI once the system is running:

| Service | Swagger UI |
|---------|-----------|
| API Gateway | http://localhost:8080/swagger-ui.html |
| Auth Service | http://localhost:8081/swagger-ui.html |
| Authorization Service | http://localhost:8082/swagger-ui.html |
| Audit Log Service | http://localhost:8083/swagger-ui.html |
| ML / Policy Engine | http://localhost:8000/docs |

---
 
## Testing

Each service has unit tests and integration tests. To run all tests locally:
```bash
# Java services
./gradlew test

# Python service
cd services/ml-policy-engine
pytest tests/ -v
```

The CI pipeline runs all tests automatically on every push.

> **Deep dive:** [Definition of Done](docs/product/DEFINITION_OF_DONE_ZEROTRUST.md)
 
---
 
## Deployment

The whole system runs in containers with Docker Compose across 4 segmented networks:

| Network | Purpose | Containers |
|---------|---------|-----------|
| `public` | External traffic | frontend, api-gateway |
| `services` | Inter-service communication | microservices, kafka |
| `data` | Database access (internal) | microservices, databases |
| `monitoring` | Metrics collection (internal) | microservices, prometheus, grafana |

Only `api-gateway` (:8080) and `frontend` (:80) are exposed to the host. Every other container runs exclusively on internal networks.

> **Deep dive:** [Deployment Diagram](docs/diagrams/DEPLOYMENT_DIAGRAM.mermaid)
 
---
 
## Documentation

| Document | Description |
|----------|-------------|
| [Product Vision Board](docs/product/PRODUCT_VISION_ZEROTRUST.md) | Project overview, user profiles and goals |
| [Threat Model — STRIDE](docs/security/THREAT-MODEL_STRIDE_ZEROTRUST.md) | 9 threats identified and mitigated |
| [Technology Stack](docs/stack/TECHNOLOGY_STACK_ZEROTRUST.md) | Stack decisions with justification |
| [Detailed Architecture](docs/architecture/DETAILED_ARCHITECTURE.md) | Microservices, data flow and principles |
| [Component Diagram](docs/architecture/DETAIL_COMPONENT_DIAGRAM.md) | Components per service |
| [Deployment Diagram](docs/architecture/DETAIL_DEPLOYMENT_DIAGRAM.md) | Docker networks and containers |
| [Definition of Done](docs/product/DEFINITION_OF_DONE_ZEROTRUST.md) | 31 verification points across 3 levels |
| [ADRs](docs/adr/) | Architecture Decision Records |

---
 
## Architecture Decision Records

> **ADRs:** [`docs/adr/`](docs/adr/)
 
---
 
## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
