# Sprint Planning — Sprint 1

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Sprint:** Sprint 1  
**Duración:** 1 semana  
**Fecha inicio:** 02/04/2026  
**Fecha fin:** 08/04/2026  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. Sprint Goal

> Establecer la fundación técnica completa de la plataforma ZeroTrust: repositorio estructurado, infraestructura containerizada con redes segmentadas, pipeline de validación automática y punto de entrada operativo al sistema.

---

## 2. Capacidad del Sprint

| Concepto | Valor |
|----------|-------|
| Duración del sprint | 7 días |
| Horas disponibles por día | 5h |
| Horas totales disponibles | 35h |
| Factor de productividad (~80%) | 28h efectivas |
| Horas estimadas comprometidas | 21.5h |
| Buffer para troubleshooting | 6.5h |

---

## 3. Contexto del Sprint

Este es el primer sprint del proyecto ZeroTrust Auth Platform. Corresponde a la **Fase 1 — Fundación (Infraestructura)** del Product Backlog, que abarca la épica **EP-01 — Infraestructura / Setup del Proyecto**. Sin la base técnica que este sprint entrega, ningún microservicio puede existir: no hay dónde poner código, no hay cómo levantarlo, no hay pipeline que lo valide y no hay punto de entrada que rutee requests.

**Fase:** Fase 1 — Fundación (Infraestructura)  
**Épica:** EP-01 — Infraestructura / Setup del Proyecto

---

## 4. Historias Comprometidas

| ID | Historia | Story Points | Horas Est. | Tareas |
|----|----------|:------------:|:----------:|:------:|
| TS-01.1 | Estructura del repositorio | 3 | 6h | 4 |
| TS-01.2 | Pipeline CI/CD | 5 | 6h | 5 |
| TS-01.3 | Docker Compose con redes segmentadas | 5 | 7h | 4 |
| TS-01.4 | API Gateway mínimo | 2 | 2.5h | 3 |
| **Total** | | **15 SP** | **21.5h** | **16** |

**Velocidad estimada:** 15 SP (primer sprint — se calibrará con datos reales al finalizar).

---

## 5. Descomposición en Tareas por Historia

---

### TS-01.1 — Estructura del repositorio (3 SP | 6h)

**Story:** "Como equipo de desarrollo, necesitamos la estructura del repositorio definida y creada con organización profesional de monorepo, para que cada microservicio, la infraestructura, la documentación y los pipelines tengan su ubicación establecida antes de implementar código."

**Criterios de aceptación:**

1. El directorio `services/` contiene la estructura interna completa de los 5 servicios: `auth-service/`, `authz-service/`, `audit-log-service/` (Java + Spring Boot con packages: config, controller, service, repository, model/entity, model/dto, security o event según corresponda, exception), `ml-policy-engine/` (Python + FastAPI con módulos: api/routes, core, models, services, repositories, events) y `api-gateway/` (Java + Spring Boot con packages: config, filter, exception).
2. Cada servicio Java incluye su `Dockerfile`, `build.gradle`, `README.md` y la estructura de tests paralela a `main/`. El servicio Python incluye `Dockerfile`, `requirements.txt`, `.flake8`, `README.md` y carpeta `tests/`.
3. El directorio `frontend/` contiene la estructura base de React: `src/` (components, pages, services, hooks, utils), `public/`, `package.json`, `Dockerfile` y `README.md`.
4. El directorio `infra/` contiene las subcarpetas de configuración: `kafka/`, `prometheus/`, `grafana/provisioning/` (datasources, dashboards) y `db/` con scripts de inicialización para cada base de datos (`postgres-authn/`, `postgres-authz/`, `mongodb-audit/`, `mongodb-ml/`).
5. El directorio `docs/` está organizado en subcarpetas por dominio: `architecture/` (Arquitectura Detallada, Diagrama de Componentes, Diagrama de Despliegue), `security/` (Threat Model STRIDE), `project/` (Product Vision Board, Product Backlog, Definition of Done), `stack/` (Technology Stack), `adr/` con su `README.md` índice, e `images/`.
6. El directorio `.github/` contiene `workflows/ci.yml` (placeholder), `ISSUE_TEMPLATE/` (bug_report.md, feature_request.md) y `pull_request_template.md`.
7. El directorio `scripts/` contiene los scripts de utilidad: `generate-keys.sh`, `seed-data.sh`, `verify-audit-chain.sh` (placeholders con comentario descriptivo).
8. La raíz del repositorio contiene: `.gitignore`, `.env.example`, `.editorconfig`, `docker-compose.yml` (placeholder), `README.md`, `LICENSE`, `settings.gradle`, `build.gradle` (raíz), y el Gradle Wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`).
9. El `README.md` raíz documenta la estructura de carpetas del proyecto con una descripción de cada directorio principal y las instrucciones básicas para clonar y levantar el sistema.
10. La estructura está subida al repositorio de GitHub con Conventional Commits.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-01.1.1 | Crear estructura de carpetas completa, `.gitkeep` en carpetas vacías y mover documentación de Sprint 0 a subcarpetas nuevas de `docs/` | 1h | | |
| T-01.1.2 | Crear archivos de configuración de la raíz: `.gitignore` (multi-lenguaje: Java, Python, Node, Docker, IDE), `.editorconfig`, `.env.example`, `settings.gradle`, `build.gradle` raíz para monorepo y generar Gradle Wrapper | 2h | | |
| T-01.1.3 | Crear archivos `.github/`: `workflows/ci.yml` (placeholder), `ISSUE_TEMPLATE/bug_report.md`, `ISSUE_TEMPLATE/feature_request.md` y `pull_request_template.md` | 1h | | |
| T-01.1.4 | Redactar `README.md` raíz documentando la estructura completa del repositorio con descripción de cada directorio principal e instrucciones básicas | 2h | | |

---

### TS-01.2 — Pipeline CI/CD (5 SP | 6h)

**Story:** "Como equipo de desarrollo, necesitamos un pipeline de integración continua, para que cada push al repositorio ejecute automáticamente linting, pruebas y build sin intervención manual."

**Criterios de aceptación:**

1. El pipeline de GitHub Actions se activa automáticamente en cada push al repositorio.
2. El pipeline ejecuta linting con Checkstyle para servicios Java (auth-service, authz-service, audit-log-service) y Flake8 para el servicio Python (ml-policy-engine).
3. El pipeline ejecuta pruebas unitarias con JUnit para servicios Java y Pytest para el servicio Python.
4. El pipeline construye las imágenes Docker de los servicios modificados.
5. Si cualquier paso falla (linting, pruebas o build), el pipeline se detiene y reporta el error.
6. El pipeline se ejecuta exitosamente sobre la estructura base del repositorio sin errores.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-01.2.1 | Crear el workflow base de GitHub Actions (`ci.yml`) con trigger en cada push y estructura de jobs por etapa | 1h | | |
| T-01.2.2 | Configurar linting con Checkstyle para los 3 servicios Java: plugin en `build.gradle`, reglas de estilo y ejecución en el pipeline | 1.5h | | |
| T-01.2.3 | Configurar linting con Flake8 para ml-policy-engine: instalación, configuración de reglas en `.flake8` y ejecución en el pipeline | 0.5h | | |
| T-01.2.4 | Configurar ejecución de pruebas unitarias: JUnit (`./gradlew test`) para servicios Java y Pytest para servicio Python en el pipeline | 1h | | |
| T-01.2.5 | Configurar build condicional de imágenes Docker: detección de servicios modificados por paths y build solo de los afectados | 2h | | |

---

### TS-01.3 — Docker Compose con redes segmentadas (5 SP | 7h)

**Story:** "Como equipo de desarrollo, necesitamos un Docker Compose que levante toda la infraestructura del sistema con un solo comando, para que los microservicios puedan desarrollarse y probarse en un entorno reproducible con redes aisladas."

**Criterios de aceptación:**

1. Las 4 redes Docker están definidas: `public`, `services`, `data`, `monitoring`.
2. Los contenedores de infraestructura base están configurados: PostgreSQL x2 (authn, authz), MongoDB x2 (audit, ml), Redis, Kafka.
3. Los volúmenes de persistencia están definidos para cada base de datos, Kafka, Prometheus y Grafana según el Deployment Diagram.
4. Solo `frontend` (:80) y `api-gateway` (:8080) exponen puertos al host. El resto opera exclusivamente en redes internas.
5. Cada contenedor está asignado a las redes correctas según el Deployment Diagram: los microservicios pertenecen a `services`, `data` y `monitoring`; las bases de datos solo a `data`; Kafka solo a `services`.
6. El comando `docker compose up` levanta todos los contenedores sin errores y los healthchecks reportan estado saludable.
7. Los contenedores de Prometheus y Grafana están configurados en la red `monitoring`.

**Estrategia de implementación:** Se construye por capas funcionales — cada grupo de contenedores se configura completo (contenedor + red + volumen + healthcheck) y se verifica con `docker compose up` antes de agregar el siguiente grupo. Esto garantiza que cualquier error está siempre en lo último que se agregó.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-01.3.1 | Configurar capa `data`: PostgreSQL x2 (authn, authz), MongoDB x2 (audit, ml) y Redis — contenedores, red `data`, volúmenes de persistencia, healthchecks y scripts de inicialización | 2h | | |
| T-01.3.2 | Configurar capa de mensajería: Kafka (con Zookeeper o KRaft) — contenedor, red `services`, volumen `kafka_data` y healthcheck | 2h | | |
| T-01.3.3 | Configurar microservicios + Gateway: Dockerfiles multi-stage (Java con Gradle y Python), asignación a redes múltiples (services + data + monitoring), dependencias `depends_on` con healthchecks, puertos expuestos solo para frontend (:80) y gateway (:8080) | 2h | | |
| T-01.3.4 | Configurar capa `monitoring`: Prometheus (con `prometheus.yml` apuntando a servicios) y Grafana (con datasource y provisioning) — contenedores, red `monitoring`, volúmenes | 1h | | |

---

### TS-01.4 — API Gateway mínimo (2 SP | 2.5h)

**Story:** "Como sistema, necesito un API Gateway que rutee los requests entrantes al microservicio correspondiente, para que los servicios internos no estén expuestos directamente al exterior."

**Criterios de aceptación:**

1. El API Gateway está implementado con Spring Boot y desplegado como contenedor Docker en las redes `public` y `services`.
2. El Gateway rutea requests a los microservicios internos por ruta: `/api/auth/**` → auth-service, `/api/authz/**` → authz-service, `/api/audit/**` → audit-log-service.
3. El Gateway expone un endpoint `/health` que retorna HTTP 200 confirmando que el servicio está operativo.
4. Los requests a rutas no definidas retornan HTTP 404 con un mensaje genérico (sin exponer detalles internos del sistema).
5. El Gateway es accesible en el puerto 8080 del host y rutea correctamente a los servicios internos dentro de la red `services`.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-01.4.1 | Crear proyecto Spring Boot con Spring Initializr, Dockerfile y configuración del contenedor en Docker Compose (redes `public` + `services`, puerto 8080) | 1h | | |
| T-01.4.2 | Configurar ruteo declarativo en `application.yml`: `/api/auth/**` → auth-service:8081, `/api/authz/**` → authz-service:8082, `/api/audit/**` → audit-log-service:8083 | 0.5h | | |
| T-01.4.3 | Implementar endpoint `/health` (HTTP 200), manejo global de rutas no definidas (HTTP 404 con mensaje genérico) y verificación de funcionamiento completo del ruteo | 1h | | |

---

## 6. Orden de Ejecución Recomendado

Las historias tienen dependencias entre sí. El orden recomendado de ejecución es:

| Orden | Historia | Justificación |
|:-----:|----------|---------------|
| 1 | TS-01.1 — Estructura del repositorio | Sin estructura no hay dónde poner código ni configuraciones. |
| 2 | TS-01.2 — Pipeline CI/CD | Una vez que existe el repositorio, el pipeline valida cada push desde el inicio. |
| 3 | TS-01.3 — Docker Compose con redes segmentadas | Con el repo estructurado y el pipeline activo, se construye la infraestructura completa. |
| 4 | TS-01.4 — API Gateway mínimo | Requiere que Docker Compose exista para integrarse como contenedor y que los servicios internos estén definidos para rutear hacia ellos. |

---

## 7. Riesgos Identificados

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|:------------:|:-------:|------------|
| Kafka difícil de configurar en Docker (contenedor no levanta, healthcheck falla) | Alta | Media | Buffer de 6.5h disponible. Documentación oficial de Kafka Docker como referencia. |
| GitHub Actions con trial and error por falta de experiencia avanzada | Media | Baja | Tareas separadas por tecnología — un fallo en Checkstyle no bloquea Flake8. |
| Gradle monorepo requiere aprendizaje (`settings.gradle`, `build.gradle` raíz) | Media | Baja | 2h asignadas a T-01.1.2 para cubrir la curva de aprendizaje. |
| Dockerfiles multi-stage para Java + Python con patrones distintos | Baja | Baja | Se configura un Dockerfile y se replica el patrón para los demás servicios del mismo lenguaje. |

---

## 8. Definition of Done del Sprint

Al finalizar el sprint, el incremento debe cumplir:

1. Todas las historias cumplen el DoD de Historia (Nivel 1) aplicable — se excluyen los puntos de testing de seguridad y Kafka que no aplican a historias de infraestructura.
2. `docker compose up` levanta los 14 contenedores sin errores con healthchecks saludables.
3. El pipeline de GitHub Actions ejecuta linting, pruebas y build exitosamente.
4. El API Gateway rutea correctamente a los servicios internos y responde en `/health`.
5. La estructura del repositorio está completa y documentada en el `README.md`.

---

## 9. Resumen

| Concepto | Valor |
|----------|-------|
| Sprint | Sprint 1 |
| Duración | 1 semana (02/04/2026 — 08/04/2026) |
| Sprint Goal | Fundación técnica completa de la plataforma |
| Épica | EP-01 — Infraestructura / Setup del Proyecto |
| Historias comprometidas | 4 |
| Story Points comprometidos | 15 SP |
| Horas estimadas | 21.5h |
| Horas efectivas disponibles | 28h |
| Buffer disponible | 6.5h |
| Total de tareas | 16 |

---

## Referencias

- [Scrum Guide — Sprint Planning](https://scrumguides.org/scrum-guide.html#sprint-planning)
- [Product Backlog — ZeroTrust Auth Platform](../project/PRODUCT_BACKLOG_ZEROTRUST.md)
- [Definition of Done — ZeroTrust Auth Platform](../project/DEFINITION_OF_DONE_ZEROTRUST.md)
- [Deployment Diagram — ZeroTrust Auth Platform](../architecture/DEPLOYMENT_DIAGRAM.md)
