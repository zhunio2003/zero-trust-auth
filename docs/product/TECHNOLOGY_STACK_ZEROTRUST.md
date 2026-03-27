# Technology Stack — ZeroTrust Auth Platform

**Versión:** 1.0  
**Fecha:** 26-03-2026  
**Autor:** Miguel Angel Zhunio Remache  

---

## Criterios de selección

> Las decisiones del stack se basaron en estándares de seguridad comprobados por décadas en la industria, ecosistemas amplios de herramientas con grandes comunidades, y rendimiento bajo carga concurrente, considerando que el dominio es IAM enterprise.

---

## Stack por capa

---

### 1. Backend Principal — Java + Spring Boot

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Gestiona el flujo de autenticación y autorización central de la plataforma — emisión de tokens, MFA, rate limiting y exposición de la API REST. |
| **Justificación técnica** | Fuertemente tipado y compilado lo que permite identificar los errores al momento de compilar |
| **Alternativas consideradas** | Node.js con Express descartado por su naturaleza dinámicamente tipada, lo que representa un riesgo en el manejo de tokens y permisos en un sistema IAM enterprise. |

---

### 2. ML / Policy Engine — Python + FastAPI

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Ejecuta el motor de detección de anomalías con ML y evalúa las políticas ABAC, calculando el nivel de confianza de cada request en base a rol, contexto e historial de comportamiento. |
| **Justificación técnica** | Ecosistema dominante para ML con librerías como scikit-learn y numpy sin competencia real en ese dominio. La cohesión técnica es clave: las políticas ABAC consumen directamente el score del modelo de anomalías, eliminando llamadas de red innecesarias entre servicios. |
| **Alternativas consideradas** | Java considerado para unificar el stack, descartado por romper la cohesión con el motor ML — las políticas ABAC consumen el score de anomalías directamente en el mismo proceso, evitando latencia de red en un flujo crítico. |

---

### 3. Mensajería — Apache Kafka

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Capa de mensajería asíncrona que distribuye eventos a múltiples servicios simultáneamente con retención de mensajes, garantizando que ningún evento se pierda aunque un servicio esté caído. |
| **Justificación técnica** | Retención de mensajes nativa permite que múltiples servicios — Prometheus, audit logs, motor ML — lean el mismo evento desde el mismo topic de forma independiente. RabbitMQ elimina el mensaje tras entregarlo, imposibilitando ese patrón en un sistema con múltiples consumidores por evento. |
| **Alternativas consideradas** | RabbitMQ descartado por su modelo de entrega única — el mensaje se elimina tras ser consumido, incompatible con un sistema donde múltiples servicios necesitan leer el mismo evento de autenticación. |

---

### 4. Base de datos relacional — PostgreSQL

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Almacena los datos estructurados y persistentes del sistema: cuentas de usuario, credenciales, políticas ABAC y configuración de roles. |
| **Justificación técnica** | Alto rendimiento, fiabilidad y cumplimiento estricto ACID garantizan integridad de datos en operaciones críticas como creación de usuarios y modificación de políticas de acceso. Estándar de la industria para datos relacionales en sistemas enterprise. |
| **Alternativas consideradas** | MySQL considerado, descartado por menor soporte nativo de tipos de datos complejos y características avanzadas que PostgreSQL ofrece nativamente como JSONB e índices parciales. |

---

### 5. Base de datos de documentos — MongoDB

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Almacena datos de estructura variable y alto volumen como los audit logs, donde cada evento puede contener campos distintos según el tipo de acción registrada. |
| **Justificación técnica** | Flexibilidad de esquema con modelo documental JSON permite registrar eventos heterogéneos sin alterar estructura de tablas. Escalabilidad horizontal nativa soporta el volumen masivo de logs generado en una plataforma IAM enterprise. |
| **Alternativas consideradas** | PostgreSQL considerado, descartado porque un schema rígido obligaría a alterar tablas ante cada nuevo tipo de evento, añadiendo complejidad operacional innecesaria para datos de estructura variable. |

---

### 6. Caché en memoria — Redis

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Almacena tokens de sesión y lista de tokens revocados con TTL nativo, garantizando expiración automática sin procesos de limpieza adicionales. |
| **Justificación técnica** | Velocidad in-memory elimina latencia en la verificación de tokens revocados, operación que ocurre en cada request de autenticación. TTL nativo de Redis expira tokens automáticamente sin jobs de mantenimiento, crítico para el mecanismo de revocación inmediata documentado en la amenaza E-03 del Threat Model. |
| **Alternativas consideradas** | PostgreSQL considerado, descartado por ausencia de TTL nativo y latencia de disco inaceptable para una verificación que ocurre miles de veces por segundo. |

---

### 7. Observabilidad — Prometheus + Grafana + OpenTelemetry

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Instrumenta, recopila y visualiza métricas críticas del sistema en tiempo real: latencia de endpoints, intentos de autenticación fallidos, estado de sesiones activas y alertas del motor de detección de anomalías. |
| **Justificación técnica** | Estándar de la industria para observabilidad en sistemas distribuidos. OpenTelemetry instrumenta el código de forma vendor-neutral, Prometheus almacena las métricas y Grafana las visualiza. En un sistema IAM enterprise la observabilidad en tiempo real no es opcional — un pico de logins fallidos puede indicar un ataque de credential stuffing activo. |
| **Alternativas consideradas** | ELK Stack (Elasticsearch + Logstash + Kibana) considerado, descartado por mayor complejidad operacional y consumo de recursos. Prometheus + Grafana es más liviano y el estándar dominante para métricas en arquitecturas de microservicios con Spring Boot. |

---

### 8. Contenedores — Docker + Docker Compose

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Dockerizar y orquestar servicios |
| **Justificación técnica** | Sistema con múltiples servicios heterogéneos — Java, Python, Kafka, PostgreSQL, MongoDB, Redis — que necesitan comunicarse en una red controlada. Docker Compose orquesta todos los servicios localmente con un solo comando, garantizando entornos reproducibles y eliminando el problema de "funciona en mi máquina". |
| **Alternativas consideradas** | Kubernetes considerado, descartado por over-engineering para un proyecto de portfolio. Kubernetes es el estándar para producción a escala enterprise, pero Docker Compose cubre perfectamente el entorno de desarrollo y demostración. Migración a Kubernetes está documentada como evolución natural del proyecto. |

---

## Resumen del stack

| Capa | Tecnología | Decisión clave |
|---|---|---|
| Backend principal | Java + Spring Boot | Spring Security, estándar IAM enterprise |
| ML / Policy engine | Python + FastAPI | Ecosistema dominante para ML, cohesión con motor de anomalías |
| Mensajería | Apache Kafka | Retención de mensajes para múltiples consumidores por evento |
| Base de datos relacional | PostgreSQL | Datos estructurados con garantías ACID |
| Base de datos documentos | MongoDB | Schema flexible para datos de estructura variable y alto volumen |
| Caché / sesiones | Redis | TTL nativo y velocidad in-memory para verificación de tokens |
| Observabilidad | Prometheus + Grafana + OpenTelemetry | Estándar de la industria para métricas en microservicios |
| Contenedores | Docker + Docker Compose | Orquestación reproducible de servicios heterogéneos |

---

## Referencias

- [Spring Security Docs](https://docs.spring.io/spring-security/reference/)
- [FastAPI Docs](https://fastapi.tiangolo.com/)
- [Apache Kafka Docs](https://kafka.apache.org/documentation/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [MongoDB Docs](https://www.mongodb.com/docs/)
- [Redis Docs](https://redis.io/docs/)
- [OpenTelemetry Docs](https://opentelemetry.io/docs/)
- [Prometheus Docs](https://prometheus.io/docs/)
