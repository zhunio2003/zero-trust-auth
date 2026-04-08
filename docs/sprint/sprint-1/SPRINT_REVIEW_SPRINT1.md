# Sprint Review — Sprint 1

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Sprint:** Sprint 1  
**Fase:** Fase 1 — Fundación (Infraestructura)  
**Fecha de Review:** 08 de abril de 2026  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. Sprint Goal

> Establecer la fundación técnica completa de la plataforma ZeroTrust: repositorio estructurado, infraestructura containerizada con redes segmentadas, pipeline de validación automática y punto de entrada operativo al sistema.

**Resultado:** ✅ Sprint Goal cumplido.

---

## 2. Resumen de Resultados

| Concepto | Valor |
|----------|-------|
| Technical Stories comprometidas | 4 |
| Technical Stories completadas | 4 |
| Story Points comprometidos | 15 |
| Story Points completados | 15 |
| Épica | EP-01 — Infraestructura / Setup del Proyecto |
| Duración planificada | 7 días (02/04/2026 — 08/04/2026) |
| Duración real | 5 días efectivos de trabajo |
| **Velocidad establecida (línea base)** | **15 SP** |

> Esta es la primera velocidad registrada del proyecto. Servirá como línea base para la planificación del Sprint 2. Los días 3, 4 y 5 del sprint no tuvieron actividad — el sprint se completó el día 8 al mediodía sin complicaciones.

---

## 3. Incremento Entregado

### TS-01.1 — Estructura del Repositorio
**Estado:** ✅ Done  
**Story Points:** 3

**Evidencia presentada:**
- Monorepo Gradle inicializado con estructura profesional de carpetas:
  - `services/` — 5 servicios inicializados: `auth-service`, `authz-service`, `audit-log-service`, `ml-policy-engine` (Python + FastAPI) y `api-gateway` (Java + Spring Boot)
  - `docs/` — documentación completa de Sprint 0 organizada por dominio (architecture, security, product, stack, adr)
  - `config/` — configuraciones globales del proyecto (Checkstyle)
  - `infra/` — configuraciones de infraestructura (Prometheus, Grafana, scripts de base de datos)
  - `.github/` — workflows de CI/CD y plantillas de Issues y Pull Requests
  - `scripts/` — scripts de utilidad (generate-keys, seed-data, verify-audit-chain)
  - `frontend/` — estructura base para el Admin Dashboard (Sprint futuro)
- Configuración del monorepo Gradle con `settings.gradle` y `build.gradle` raíz compartido para los 4 servicios Java
- Archivos de configuración de la raíz: `.gitignore` multi-lenguaje, `.editorconfig`, `.env.example` con todas las variables del sistema
- `README.md` completo con descripción del proyecto, features, modelo de seguridad, tech stack, estructura de carpetas y guía de inicio
- Plantillas de GitHub: `bug_report.md`, `feature_request.md` y `pull_request_template.md`

---

### TS-01.2 — Pipeline CI/CD
**Estado:** ✅ Done  
**Story Points:** 5

**Evidencia presentada:**
- Workflow `ci.yml` creado en `.github/workflows/` con 5 jobs organizados en dos carriles paralelos:

```
Lint Java ──→ Test Java ──→
                            Build Docker (solo servicios modificados)
Lint Python ──→ Test Python ──→
```

- `lint-java` — Checkstyle con reglas Google Style sobre los 4 servicios Java
- `lint-python` — Flake8 sobre `ml-policy-engine`
- `test-java` — JUnit con upload de reportes (`if: always()`)
- `test-python` — Pytest con upload de reportes
- `build-docker` — Build condicional con `strategy.matrix` y `git diff` para detectar y buildear solo los servicios modificados en cada push

**Estado actual del pipeline:**
- Pipeline configurado y funcional en GitHub Actions. Los checks reportan `✗ 0/5` en los commits actuales — falla esperada porque los microservicios no tienen implementación real todavía. El pipeline se pondrá en verde progresivamente conforme Sprint 2 implemente cada servicio.

**Archivos de configuración de calidad de código:**
- `config/checkstyle/checkstyle.xml` con reglas Google Style para Java
- `build.gradle` raíz con plugin de Checkstyle aplicado a todos los subproyectos Java vía `subprojects {}`

---

### TS-01.3 — Docker Compose con Redes Segmentadas
**Estado:** ✅ Done  
**Story Points:** 5

**Evidencia presentada:**
- `docker-compose.yml` con 14 contenedores organizados en 4 redes Docker aisladas:

| Red | Tipo | Contenedores |
|-----|------|-------------|
| `public` | Externa | frontend, api-gateway |
| `services` | Externa | microservicios, kafka |
| `data` | **Interna** | bases de datos, redis |
| `monitoring` | **Interna** | prometheus, grafana |

- 8 contenedores de infraestructura verificados en estado `healthy` mediante `docker compose ps`:

| Contenedor | Imagen | Estado |
|------------|--------|--------|
| postgres-authn | postgres:17 | ✅ healthy |
| postgres-authz | postgres:17 | ✅ healthy |
| mongodb-auditlog | mongo:8 | ✅ healthy |
| mongodb-ml | mongo:8 | ✅ healthy |
| redis | redis:8 | ✅ healthy |
| kafka | apache/kafka:3.9.0 | ✅ healthy |
| prometheus | prom/prometheus:latest | ✅ healthy |
| grafana | grafana/grafana:latest | ✅ healthy |

- 6 contenedores pendientes (4 microservicios + api-gateway + frontend) — no levantaron porque sus Dockerfiles no existen todavía. Healthchecks configurados y listos para cuando se implementen.
- Kafka en **KRaft mode** (sin Zookeeper) — estándar moderno desde Kafka 3.x
- `infra/prometheus/prometheus.yml` creado con scrape targets para los 4 microservicios

**Incidencias resueltas durante el sprint:**

| Problema | Causa | Solución |
|----------|-------|----------|
| Kafka crasheaba al arrancar | `KAFKA_LISTENERS` con `0.0.0.0` no aceptado por `apache/kafka:3.9.0` | Hostname explícito `kafka:9092` + listener `LOCAL://localhost:9094` para healthcheck interno |
| Prometheus healthcheck fallaba | La imagen de Prometheus no incluye `curl` | Cambiar a `wget --spider` |
| MongoDB healthcheck sin autenticación | Healthcheck no pasaba credenciales | Agregar `--username` y `--password` al comando `mongosh` |

---

### TS-01.4 — API Gateway Mínimo
**Estado:** ✅ Done  
**Story Points:** 2

**Evidencia presentada:**
- API Gateway implementado con **Spring Cloud Gateway Server WebMVC** (Spring Cloud 2025.1.1 — compatible con Spring Boot 4.0.4)
- Ruteo declarativo configurado en `application.yml`:

| Ruta | Destino |
|------|---------|
| `/api/auth/**` | `auth-service:8081` |
| `/api/authz/**` | `authz-service:8082` |
| `/api/audit/**` | `audit-log-service:8083` |

- Endpoint `/health` expuesto mediante Actuator con `base-path: /`
- Dockerfile multi-stage implementado (eclipse-temurin:21-jdk para build, eclipse-temurin:21-jre para runtime)
- Imagen Docker buildeada exitosamente: `zerotrust/api-gateway:test`

**Verificaciones realizadas mediante `curl`:**

```bash
# Health check
curl http://localhost:8080/health
→ {"groups":["liveness","readiness"],"status":"UP"} ✅

# Ruta no definida
curl -v http://localhost:8080/ruta-inexistente
→ HTTP 404 {"status":404,"error":"Not Found","path":"/ruta-inexistente"} ✅
```

**Incidencia resuelta:**
- Spring Cloud `2025.0.0` es incompatible con Spring Boot `4.0.x`. Versión correcta: `2025.1.1` (Oakwood). El artifact también cambió de nombre: `spring-cloud-gateway-server-mvc` → `spring-cloud-starter-gateway-server-webmvc`.

---

## 4. Deuda Técnica Identificada

| ID | Descripción | Prioridad | Sprint destino |
|----|-------------|-----------|----------------|
| DT-001 | El Dockerfile de cada servicio Java requiere copiar los `build.gradle` de todos los subproyectos del monorepo para que Gradle resuelva correctamente las dependencias. A futuro se evaluará migrar cada servicio a repositorio independiente o ajustar la estructura del monorepo para simplificar los builds en Docker. | Baja | Por definir |

---

## 5. Velocidad del Equipo

| Sprint | SP Comprometidos | SP Completados | Días efectivos |
|--------|-----------------|----------------|----------------|
| Sprint 1 | 15 | 15 | 5 de 7 disponibles |

> **Línea base establecida: 15 Story Points por sprint.** El sprint se completó al mediodía del día 8 con 2 días sin actividad (días 3, 4 y 5). La estimación de 21.5h fue conservadora — el trabajo real fue más eficiente. Este valor se usará como referencia para el Sprint Planning del Sprint 2.

---

## 6. Adaptaciones al Product Backlog

| Tipo | Descripción |
|------|-------------|
| Deuda técnica | DT-001 registrada para seguimiento futuro |
| Sin historias diferidas | Todas las Technical Stories del sprint se completaron |

---

## 7. Próximos Pasos

1. **Sprint Retrospective** — reflexión sobre el proceso del Sprint 1.
2. **Sprint Planning Sprint 2** — planificación con velocidad base de 15 SP.
3. **Iniciar EP-02 — Authentication Service** — primer microservicio del flujo principal: registro de usuarios, login con MFA, emisión de JWT firmados con RS256, rate limiting con Token Bucket y publicación de eventos a Kafka.

---

## Referencias

- [Product Backlog — ZeroTrust Auth Platform](../product/PRODUCT_BACKLOG_ZEROTRUST.md)
- [Sprint Planning Sprint 1](../product/SPRINT_PLANNING_SPRINT1.md)
- [Definition of Done](../product/DEFINITION_OF_DONE_ZEROTRUST.md)
- [Repositorio GitHub](https://github.com/zhunio2003/zero-trust-auth)
