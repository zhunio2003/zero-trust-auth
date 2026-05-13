<div align="center">
    <h1>ZEROTRUST — Enterprise Authentication & Authorization Platform</h1>
<img src="docs/brand/logo-zero.png" alt="logo zerotrust" width="900">
<br/><br/>

<img src="https://img.shields.io/badge/React-planned-61DAFB?logo=react&logoColor=black" alt="React"/>
<br/>

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

La plataforma ZeroTrust Auth es una API de autenticación y autorización de nivel empresarial, desarrollada desde cero con una arquitectura de microservicios. Implementa el modelo Zero Trust real: ninguna solicitud se considera confiable por defecto. Cada sesión se verifica continuamente en función de la identidad, el contexto y el comportamiento.

La plataforma ofrece mecanismos de seguridad multicapa, incluyendo MFA, gestión del ciclo de vida de los tokens JWT y limitación de velocidad distribuida. Un motor de detección de anomalías basado en aprendizaje automático monitoriza todos los eventos de autenticación y autorización en tiempo real, calculando una puntuación de confianza por solicitud y alertando a los administradores cuando surgen patrones sospechosos. La observabilidad completa está integrada desde el principio: métricas, datos históricos y un panel de administración en tiempo real proporcionan visibilidad total del sistema en todo momento.

---
 
## Architecture Overview

> **Deep dive:** [Arquitectura Detallada](docs/architecture/DETAILED_ARCHITECTURE.md) · [Diagrama de Componentes](docs/diagrams/COMPONENT_DIAGRAM.mermaid)

---
 
## Key Features

- **Multi-Factor Authentication (MFA)** — soporta TOTP (códigos temporales) y WebAuthn/Passkeys vinculados al dominio legítimo
- **JWT Lifecycle Management** — tokens firmados con RS256, rotación de refresh tokens y revocación inmediata con blacklist en Redis
- **Distributed Rate Limiting** — algoritmo Token Bucket implementado desde cero con contadores distribuidos entre instancias
- **ABAC Authorization** — autorización basada en atributos evaluando rol, departamento, hora, sensibilidad del recurso y score de confianza del ML
- **ML Anomaly Detection** — cálculo de score de confianza en tiempo real basado en IP, patrones de comportamiento, velocidad de requests e historial del usuario
- **Immutable Audit Logs** — hash chaining garantiza que cada evento es detectable ante cualquier modificación y tiene valor forense
- **Real-time Observability** — métricas con Prometheus, dashboards en Grafana y alertas automáticas ante patrones anómalos detectados
- **Admin Dashboard** — visibilidad completa de sesiones activas, audit logs, políticas ABAC y monitoreo de amenazas en tiempo real

---

## Security Model

Esta plataforma implementa un modelo de confianza cero real, creado desde cero: ninguna solicitud se considera de confianza por defecto, y cada acceso se verifica continuamente.
La seguridad se aplica en múltiples niveles:

- **Identity verification** — MFA obligatorio con TOTP y WebAuthn. Sin segundo factor no hay acceso.
- **Token security** — JWTs firmados con RS256. Tokens con algoritmo none o firmas inválidas son rechazados explícitamente. Revocación inmediata en Redis ante cierre de sesión o detección de token reuse attack.
- **Authorization** — políticas ABAC evaluadas en cada request combinando atributos del usuario, contexto y score de confianza del ML. Denegación por defecto ante cualquier caso no cubierto.
- **Rate limiting** — algoritmo Token Bucket distribuido limita requests por IP. Primer filtro antes de cualquier verificación de credenciales.
- **Audit integrity** — hash chaining en cada evento de auditoría. Cualquier modificación rompe la cadena y es detectable.
- **Error handling** — mensajes de error genéricos al cliente. Ninguna respuesta expone estructura interna, nombres de tablas ni stack traces.
- **Transport** — CORS estricto configurado en el API Gateway. WebAuthn vincula la autenticación al dominio legítimo.

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

El sistema se ejecuta completamente en contenedores con Docker Compose a través de 4 redes segmentadas:

| Network | Purpose | Containers |
|---------|---------|-----------|
| `public` | External traffic | frontend, api-gateway |
| `services` | Inter-service communication | microservices, kafka |
| `data` | Database access (internal) | microservices, databases |
| `monitoring` | Metrics collection (internal) | microservices, prometheus, grafana |

Solo `api-gateway` (:8080) y `frontend` (:80) están expuestos al host. Todos los demás contenedores operan exclusivamente en redes internas.

> **Deep dive:** [Diagrama de Despliegue](docs/diagrams/DEPLOYMENT_DIAGRAM.mermaid)
 
---
 
## Documentation
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
