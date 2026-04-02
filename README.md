<div align="center">
    <h1>ZEROTRUST — Plataforma de Predicción Especializada</h1>
    <img src="docs/brand/logo-zerotrust.png" alt="logo zerotrust" width="900">
    <br/><br/>
    <img src="https://img.shields.io/badge/React-planned-61DAFB?logo=react&logoColor=black" alt="React"/>    <br/>
    <img src="https://img.shields.io/badge/Java-21-orange" alt="Java"/>
    <img src="https://img.shields.io/badge/Spring%20Boot-4.0.4-green" alt="Spring Boot"/>
    <img src="https://img.shields.io/badge/Python-3.12.4-blue" alt="Python"/>
    <img src="https://img.shields.io/badge/FastAPI-latest-teal" alt="FastAPI"/>
    <br/>
    <img src="https://img.shields.io/badge/MongoDB-8-47A248?logo=mongodb&logoColor=white" alt="MongoDB"/>
    <img src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
    <img src="https://img.shields.io/badge/Redis-8-DC382D?logo=redis&logoColor=white" alt="Redis"/>
    <img src="https://img.shields.io/badge/Apache%20Kafka-3.7-231F20?logo=apachekafka&logoColor=white" alt="Apache Kafka"/>
    <br/>
    <img src="https://img.shields.io/badge/Prometheus-2.53-E6522C?logo=prometheus&logoColor=white" alt="Prometheus"/>
    <img src="https://img.shields.io/badge/Grafana-11-F46800?logo=grafana&logoColor=white" alt="Grafana"/>
    <img src="https://img.shields.io/badge/OpenTelemetry-1.39-7B61FF?logo=opentelemetry&logoColor=white" alt="OpenTelemetry"/>
    <br/>
    <img src="https://img.shields.io/badge/Docker-Compose-2496ED" alt="Docker"/>
    <img src="https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?logo=githubactions&logoColor=white" alt="GitHub Actions"/>
    <br/><br/>
</div>

> Enterprise-grade authentication & authorization platform built from scratch with microservices architecture. Implements real Zero Trust: every request is verified continuously based on identity, context, and behavior.

---

## What is this?

---
 
## Architecture Overview

> **Deep dive:** [Arquitectura Detallada](docs/architecture/DETAILED_ARCHITECTURE.md) · [Diagrama de Componentes](docs/diagrams/COMPONENT_DIAGRAM.mermaid)

---
 
## Key Features

---

## Security Model

> **Deep dive:** [Threat Model — STRIDE](docs/security/THREAT-MODEL_STRIDE_ZEROTRUST.md)
 
---

## Tech Stack

> **Deep dive:** [Technology Stack](docs/stack/TECHNOLOGY_STACK_ZEROTRUST.md)
 
---
 
## Project Structure

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
 
Each microservice owns its own database (database-per-service principle). No service accesses another service's storage directly. Communication between services follows the synchronous flow `Gateway → AuthN → AuthZ → Policy Engine`, with asynchronous event publishing to Kafka for audit logging and ML training.
 
---
 
## Getting Started

---
 
## API Reference

---
 
## Testing

> **Deep dive:** [Definition of Done](docs/product/DEFINITION_OF_DONE_ZEROTRUST.md)
 
---
 
## Deployment

> **Deep dive:** [Diagrama de Despliegue](docs/diagrams/DEPLOYMENT_DIAGRAM.mermaid)
 
---
 
## Documentation

---
 
## Architecture Decision Records

> **ADRs:** [`docs/adr/`](docs/adr/)
 
---
 
## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.