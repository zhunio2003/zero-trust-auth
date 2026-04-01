# Product Backlog — Épicas

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Versión:** 1.0  
**Fecha:** 2026-04-01  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. Introducción

Este documento define las **épicas** del Product Backlog del sistema ZeroTrust Auth Platform, organizadas en fases de desarrollo priorizadas. Las épicas fueron derivadas a partir del análisis de los perfiles de usuario definidos en el Product Vision Board, los microservicios establecidos en la Arquitectura Detallada y las amenazas identificadas en el Threat Model STRIDE.

**Documentos de referencia:**

- `docs/zerotrust-auth-platform.md` — Product Vision Board con 3 perfiles de usuario (Tech Lead, Backend Developer, System Administrator).
- `docs/DETAILED_ARCHITECTURE.md` — Arquitectura detallada con 4 microservicios y principios arquitectónicos.
- `docs/COMPONENT_DIAGRAM.md` — Diagrama de componentes por servicio.
- `docs/DEPLOYMENT_DIAGRAM.md` — Diagrama de despliegue con redes Docker segmentadas.
- `docs/THREAT-MODEL_STRIDE.md` — Modelo de amenazas STRIDE con 9 amenazas identificadas.
- `docs/TECHNOLOGY_STACK_ZEROTRUST.md` — Decisiones tecnológicas del proyecto.

---

## 2. Criterios de Priorización

Las épicas se organizaron siguiendo la cadena de dependencias del flujo principal del sistema: `Cliente → API Gateway → AuthN → AuthZ → Policy Engine`, donde cada capa depende de la anterior para funcionar.

| Fase | Pregunta clave | Enfoque |
|------|----------------|---------|
| Fase 1 | ¿Qué necesito para que el sistema exista? | Infraestructura técnica base |
| Fase 2 | ¿Qué necesito para que el flujo principal funcione? | Autenticación, autorización y detección de anomalías |
| Fase 3 | ¿Qué necesito para trazabilidad y operación? | Auditoría y administración |

---

## 3. Épicas del Sistema

Se identificaron **6 épicas** clasificadas en tres categorías según su origen:

### 3.1 Épica Técnica (Infraestructura)

Technical Stories que no aportan valor directo al usuario pero son imprescindibles para que la plataforma funcione. Incluye la estructura del repositorio, Docker Compose, CI/CD, redes Docker y el API Gateway mínimo (ruteo básico).

| ID | Épica | Componentes involucrados |
|----|-------|--------------------------|
| EP-01 | Infraestructura / Setup del Proyecto | Repositorio GitHub, Docker Compose, GitHub Actions (CI/CD), redes Docker (public, services, data, monitoring), API Gateway (ruteo básico) |

> **Nota sobre el API Gateway:** No tiene épica propia. Un Gateway mínimo con ruteo básico se construye como parte de esta épica. Las funcionalidades adicionales (CORS estricto, headers de seguridad) se agregan incrementalmente como Technical Stories dentro de las épicas que las necesiten.

### 3.2 Épicas del Flujo Principal

Derivadas de los 4 microservicios del sistema y del flujo sincrónico principal `Gateway → AuthN → AuthZ → Policy Engine`. Cada épica mapea directamente a un microservicio de la Arquitectura Detallada.

| ID | Épica | Descripción | Microservicio |
|----|-------|-------------|---------------|
| EP-02 | Autenticación | Registro de usuarios, login con MFA (TOTP / WebAuthn), emisión y ciclo de vida de tokens JWT (RS256), rate limiting con Token Bucket, revocación de sesiones y detección de token reuse attack. | Authentication Service |
| EP-03 | Autorización | Evaluación de acceso basada en atributos (ABAC) con extracción de claims del JWT, consulta de políticas y delegación al Policy Engine para decisión final con score de anomalías. | Authorization Service |
| EP-04 | Detección de Anomalías | Entrenamiento de modelo ML con datos históricos de comportamiento, cálculo de score de confianza por request, evaluación de políticas ABAC consumiendo el score del ML en el mismo proceso. | ML / Anomaly Detection + Policy Engine |
| EP-05 | Auditoría | Consumo de eventos de Kafka, persistencia inmutable con hash chaining, API de consulta con filtros y verificación de integridad de la cadena de hashes. | Audit Log Service |

### 3.3 Épica de Interfaz

Derivada de la necesidad del System Administrator de gestionar la plataforma visualmente.

| ID | Épica | Descripción | Usuario principal |
|----|-------|-------------|-------------------|
| EP-06 | Admin Dashboard | Interfaz web para gestión de usuarios, visualización de sesiones activas, consulta de audit logs, monitoreo de amenazas en tiempo real y gestión de políticas ABAC. | System Administrator |

---

## 4. Fases de Desarrollo

### Fase 1 — Fundación (Infraestructura)

**Objetivo:** Establecer la base técnica sin la cual ningún servicio puede existir.

| Prioridad | ID | Épica |
|-----------|----|-------|
| 1 | EP-01 | Infraestructura / Setup del Proyecto |

**Justificación:** Sin repositorio, sin Docker Compose, sin CI/CD ni redes Docker segmentadas, no hay dónde poner el código ni cómo levantarlo. El API Gateway mínimo se incluye aquí porque es el punto de entrada que rutea requests a los microservicios — necesita existir antes de que el primer servicio reciba tráfico.

---

### Fase 2 — Flujo Principal (Core del Sistema)

**Objetivo:** Construir el flujo completo de autenticación, autorización y detección de anomalías — el core de una plataforma Zero Trust.

| Prioridad | ID | Épica |
|-----------|----|-------|
| 2 | EP-02 | Autenticación |
| 3 | EP-03 | Autorización |
| 4 | EP-04 | Detección de Anomalías |

**Justificación:** La cadena de dependencias es estricta: AuthN responde "¿quién sos?" antes de que AuthZ pueda preguntar "¿podés hacer esto?", y el Policy Engine necesita que AuthZ exista para recibir la llamada sincrónica con los atributos del request. Kafka y las bases de datos correspondientes se levantan junto con cada servicio que las necesita — la infraestructura acompaña al servicio, no se construye por separado.

---

### Fase 3 — Trazabilidad y Administración

**Objetivo:** Completar la trazabilidad del sistema con audit logs inmutables y dotar al administrador de una interfaz de gestión.

| Prioridad | ID | Épica |
|-----------|----|-------|
| 5 | EP-05 | Auditoría |
| 6 | EP-06 | Admin Dashboard |

**Justificación:** El Audit Log Service es un consumidor pasivo de Kafka — no participa en el flujo sincrónico principal, pero es imprescindible para la trazabilidad y no-repudiación documentadas en el Threat Model (T-01, R-01). El Admin Dashboard va último porque consume datos de todos los servicios — entre más servicios estén funcionando, más valor aporta la interfaz de administración.

---

## 5. Trazabilidad con Threat Model

| Épica | Amenazas que mitiga |
|-------|---------------------|
| EP-01 | — (infraestructura base) |
| EP-02 | E-01 (JWT Manipulation), E-03 (Token Reuse After Revocation), S-01 (Credential Stuffing), S-02 (Phishing de tokens), D-01 (Authentication Endpoint Flooding) |
| EP-03 | E-02 (ABAC Policy Misconfiguration) |
| EP-04 | S-01 (detección de patrones anómalos), D-01 (detección de IPs anómalas), E-02 (evaluación ABAC con score ML) |
| EP-05 | T-01 (Audit Log Tampering), R-01 (Negación de acciones administrativas) |
| EP-06 | — (interfaz de gestión, no mitiga amenazas directamente) |

> La amenaza I-01 (Exposición de datos sensibles en errores) se mitiga de forma transversal en todos los servicios mediante manejo centralizado de errores — no pertenece a una épica específica.

---

## 6. Resumen

| Concepto | Valor |
|----------|-------|
| Total de épicas | 6 |
| Épicas del flujo principal | 4 |
| Épicas técnicas | 1 |
| Épicas de interfaz | 1 |
| Fases de desarrollo | 3 |
| Amenazas STRIDE cubiertas | 9/9 |
| Usuarios cubiertos | Tech Lead, Backend Developer, System Administrator |

---

## Referencias

- [Scrum Guide — Product Backlog](https://scrumguides.org/scrum-guide.html#product-backlog)
- [STRIDE Threat Model — Microsoft](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats)
- [Microservices Patterns — Chris Richardson](https://microservices.io/patterns/)
