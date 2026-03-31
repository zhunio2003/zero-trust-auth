# Arquitectura Detallada — ZeroTrust Auth Platform

**Versión:** 1.0  
**Fecha:** 2026-03-30  
**Autor:** Miguel Angel Zhunio Remache  

---

## Contexto del sistema

> Una plataforma de autenticación y autorización de nivel empresarial, construida desde cero con arquitectura de microservicios. Implementa el modelo **Zero Trust** real: ninguna solicitud es confiable por defecto, cada acceso se verifica continuamente en función de identidad, contexto y comportamiento.

---

## Principios arquitectónicos

| Principio | Aplicación |
|---|---|
| **Database per Service** | Cada microservicio es dueño exclusivo de su base de datos. Ningún servicio accede directamente al storage de otro. |
| **Alta cohesión, bajo acoplamiento** | Las responsabilidades que dependen entre sí en el mismo flujo sincrónico se co-localizan. Las que no, se desacoplan vía Kafka. |
| **Servicios por dominio, no por infraestructura** | Los microservicios emergen de verbos del dominio IAM (autenticar, autorizar, detectar, auditar), no de herramientas o bases de datos. |
| **Desacoplamiento asíncrono** | Los eventos se publican en Kafka para que múltiples consumidores los procesen de forma independiente sin bloquear el flujo principal. |

---

## Flujo principal de un request

```
Cliente → API Gateway → Authentication Service → Authorization Service → Policy Engine (Python/FastAPI) → Recurso
                              ↓                          ↓                        ↓
                           [Kafka] ←←←←←←←←←←←←←←←← [Kafka] ←←←←←←←←←←←←← [Kafka]
                              ↓                          ↓                        ↓
                        Audit Log Service          Audit Log Service        ML (actualización modelo)
```

1. El request llega al **API Gateway**, que rutea al servicio correspondiente.
2. El **Authentication Service** valida el JWT — verifica firma RS256 en memoria y consulta blacklist en Redis.
3. El **Authorization Service** extrae atributos del JWT y consulta las políticas ABAC en su PostgreSQL.
4. El Authorization Service llama sincrónicamente al **Policy Engine** (Python/FastAPI), que evalúa las políticas ABAC consumiendo el score de anomalías del módulo ML en el mismo proceso.
5. Se permite o deniega el acceso al recurso.
6. Cada servicio emite eventos a **Kafka** de forma asíncrona — el **Audit Log Service** los consume y persiste con hash chaining.

---

## Microservicios

---

### 1. Authentication Service — Java + Spring Boot

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Gestiona la identidad del usuario: registro, autenticación, emisión y ciclo de vida completo de tokens, MFA y rate limiting del endpoint de login. |
| **Pregunta que responde** | ¿Quién sos? |

#### Responsabilidades detalladas

| Responsabilidad | Descripción | Amenaza mitigada |
|---|---|---|
| Registro de usuarios | Almacena credenciales hasheadas y datos de perfil en PostgreSQL | — |
| Login (autenticación inicial) | Verifica credenciales contra PostgreSQL, ejecuta flujo MFA (TOTP / WebAuthn) | S-01: Credential Stuffing, S-02: Phishing de tokens |
| Emisión de tokens | Genera Access Token (JWT firmado con RS256) y Refresh Token almacenado en Redis | E-01: JWT Manipulation |
| Validación de tokens | Verifica firma RS256 en memoria y consulta blacklist de tokens revocados en Redis | E-01: JWT Manipulation, E-03: Token Reuse After Revocation |
| Renovación de tokens | Valida Refresh Token en Redis y emite nuevo Access Token | E-03: Token Reuse After Revocation |
| Revocación / cierre de sesión | Elimina Refresh Token de Redis inmediatamente y agrega Access Token a blacklist con TTL | E-03: Token Reuse After Revocation |
| Rate limiting | Algoritmo Token Bucket implementado desde cero con contadores distribuidos en Redis | D-01: Authentication Endpoint Flooding |
| Emisión de eventos a Kafka | Publica contexto del request (IP, timestamp, user-agent, resultado) para consumo asíncrono por Audit Log y ML | T-01, R-01: Trazabilidad de acciones |

#### Bases de datos

| Base de datos | Uso | Justificación |
|---|---|---|
| **PostgreSQL** | Datos persistentes de usuario — credenciales hasheadas, perfil, configuración MFA | Datos estructurados con relaciones claras que requieren garantías ACID. La integridad de credenciales es crítica en un sistema IAM. |
| **Redis** | Refresh Tokens activos, blacklist de Access Tokens revocados, contadores de rate limiting | TTL nativo garantiza expiración automática sin jobs de mantenimiento. Velocidad in-memory elimina latencia en la verificación de tokens revocados — operación que ocurre en cada request (E-03). Los contadores de rate limiting en Redis son distribuidos — múltiples instancias del servicio comparten el mismo estado (D-01). |

---

### 2. Authorization Service — Java + Spring Boot

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Evalúa si un usuario autenticado tiene permiso de acceder a un recurso específico, basándose en políticas ABAC que consideran atributos de usuario, contexto y score de confianza del ML. |
| **Pregunta que responde** | ¿Podés hacer esto? |

#### Responsabilidades detalladas

| Responsabilidad | Descripción | Amenaza mitigada |
|---|---|---|
| Extracción de atributos del JWT | Lee claims del usuario (rol, departamento) directamente del token — no consulta la base de datos de otro servicio | — |
| Consulta de políticas ABAC | Lee las reglas de autorización desde su PostgreSQL propio | E-02: ABAC Policy Misconfiguration |
| Llamada sincrónica al Policy Engine | Invoca al servicio Python/FastAPI para obtener la evaluación ABAC con el score de anomalías incluido | E-02: ABAC Policy Misconfiguration |
| Decisión de acceso | Permite o deniega el acceso al recurso basándose en la respuesta del Policy Engine | E-02: ABAC Policy Misconfiguration |
| Emisión de eventos a Kafka | Publica la decisión (access_granted / access_denied) con contexto completo para Audit Log | T-01, R-01: Trazabilidad de decisiones de acceso |

#### Bases de datos

| Base de datos | Uso | Justificación |
|---|---|---|
| **PostgreSQL** (instancia propia) | Políticas ABAC — reglas con condiciones, atributos, operadores y valores | Las políticas son datos estructurados con relaciones claras que requieren consistencia ACID. Una política corrupta o inconsistente es exactamente la amenaza E-02 — errores lógicos que permiten acceso no autorizado. |

#### Nota arquitectónica

El Authorization Service no almacena datos de usuario ni accede a la base de datos del Authentication Service. Los atributos del usuario viajan dentro del JWT, respetando el principio de **database per service**. Cada microservicio es dueño exclusivo de su storage.

---

### 3. ML / Anomaly Detection + Policy Engine — Python + FastAPI

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Detecta comportamiento anómalo mediante ML y evalúa políticas ABAC en el mismo proceso, calculando el nivel de confianza de cada request en base a identidad, contexto e historial de comportamiento. |
| **Pregunta que responde** | ¿Este comportamiento es normal? ¿Los atributos cumplen las políticas? |

#### Dos responsabilidades co-localizadas

Este servicio contiene dos responsabilidades en el mismo proceso Python/FastAPI por **cohesión técnica**: el Policy Engine consume directamente el score del módulo ML sin llamadas de red, eliminando latencia en un flujo sincrónico crítico. Separarlos en dos servicios generaría complejidad operacional (dos deploys, dos healthchecks, comunicación por red) sin beneficio real — ambas responsabilidades cambian juntas, se despliegan juntas y una depende directamente de la otra en cada request.

| Responsabilidad | Tipo | Descripción |
|---|---|---|
| **Detección de anomalías (ML)** | Asíncrona + Sincrónica | Consume eventos de Kafka para entrenar y actualizar el modelo. En tiempo real, calcula el score de confianza analizando el request actual contra patrones históricos del usuario. |
| **Evaluación ABAC (Policy Engine)** | Sincrónica | Recibe la llamada del Authorization Service, consume el score del ML y evalúa las políticas ABAC con todos los atributos (usuario, contexto, recurso, score) para emitir la decisión de acceso. |

#### Responsabilidades detalladas

| Responsabilidad | Descripción | Amenaza mitigada |
|---|---|---|
| Consumo de eventos de Kafka | Recibe datos de comportamiento (logins, IPs, horarios, user-agent) emitidos por el Authentication Service | — |
| Entrenamiento y actualización del modelo | Aprende patrones normales de comportamiento por usuario con datos históricos | S-01: Detección de credential stuffing por patrones anómalos |
| Almacenamiento de datos históricos | Persiste patrones de comportamiento por usuario y modelo entrenado en su MongoDB propio | — |
| Cálculo de score de confianza | Analiza el request actual contra el patrón histórico del usuario — evalúa IP, horario, velocidad de requests, user-agent | D-01: Detección de IPs con comportamiento anómalo |
| Evaluación de políticas ABAC | Consume el score del ML + atributos del JWT + contexto del request y evalúa contra las políticas para decidir acceso | E-02: ABAC Policy Misconfiguration |
| Emisión de eventos a Kafka | Publica el resultado de la evaluación con score y atributos evaluados para Audit Log | T-01, R-01: Trazabilidad de evaluaciones |

#### Bases de datos

| Base de datos | Uso | Justificación |
|---|---|---|
| **MongoDB** (instancia propia) | Datos históricos de comportamiento por usuario y modelo ML entrenado | Cada usuario tiene patrones de comportamiento con campos variables — IPs frecuentes, horarios habituales, dispositivos conocidos. La flexibilidad de esquema documental permite almacenar esta información heterogénea sin alterar estructuras. El modelo entrenado se persiste para sobrevivir reinicios del servicio. |

---

### 4. Audit Log Service — Java + Spring Boot

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Persiste todos los eventos del sistema de forma inmutable con hash chaining, garantizando trazabilidad completa y no-repudiación de acciones. |
| **Pregunta que responde** | ¿Qué pasó, quién lo hizo y cuándo? |

#### Responsabilidades detalladas

| Responsabilidad | Descripción | Amenaza mitigada |
|---|---|---|
| Consumo de eventos de Kafka | Recibe eventos de Authentication Service, Authorization Service y ML/Policy Engine de forma asíncrona | — |
| Persistencia con hash chaining | Cada evento se almacena con el hash del evento anterior — `hash_n = hash(contenido_n + hash_{n-1})`. Cualquier modificación rompe la cadena y es detectable. | T-01: Audit Log Tampering |
| Garantía de no-repudiación | Cada evento registra identidad, acción, timestamp, IP y resultado — imposible negar acciones realizadas | R-01: Negación de acciones administrativas |
| API de consulta | Expone endpoints para que el dashboard de administración consulte logs con filtros (por usuario, fecha, tipo de acción, severidad) | — |
| Verificación de integridad | Permite validar que la cadena de hashes no fue alterada recalculando los hashes secuencialmente | T-01: Audit Log Tampering |

#### Bases de datos

| Base de datos | Uso | Justificación |
|---|---|---|
| **MongoDB** (instancia propia) | Eventos de auditoría inmutables con hash chaining | Esquema flexible — cada tipo de evento tiene campos distintos: un login registra IP y user-agent, una modificación de política registra la regla anterior y la nueva, un acceso denegado registra el recurso y el motivo. Escalabilidad horizontal nativa soporta el volumen masivo de eventos generado en un sistema IAM enterprise. |

---

## Infraestructura de soporte

> Los siguientes componentes no son microservicios — no contienen lógica de negocio del dominio IAM. Son herramientas que los microservicios utilizan para comunicarse, almacenar datos, monitorearse y desplegarse.

---

### API Gateway

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Punto de entrada único al sistema. Rutea requests al microservicio correspondiente. |
| **Justificación** | Centraliza el acceso, elimina la exposición directa de microservicios al exterior y permite gestionar cross-cutting concerns (CORS, headers de seguridad) en un solo punto. |

---

### Apache Kafka

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Capa de mensajería asíncrona que distribuye eventos a múltiples consumidores simultáneamente con retención de mensajes. |
| **Justificación** | Cada evento de autenticación o autorización debe ser consumido por múltiples servicios de forma independiente (Audit Log, ML). La retención de mensajes de Kafka permite que cada consumidor lea a su propio ritmo sin perder eventos, incluso si un servicio estuvo caído. |
| **Productores** | Authentication Service, Authorization Service, ML/Policy Engine |
| **Consumidores** | Audit Log Service, ML/Anomaly Detection |

---

### PostgreSQL

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Almacena datos estructurados y persistentes con garantías ACID. |
| **Instancias** | Dos instancias independientes — una para Authentication Service (usuarios, credenciales, configuración MFA) y otra para Authorization Service (políticas ABAC). Cada servicio es dueño exclusivo de su instancia. |

---

### MongoDB

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Almacena datos de estructura variable y alto volumen con esquema flexible. |
| **Instancias** | Dos instancias independientes — una para Audit Log Service (eventos inmutables con hash chaining) y otra para ML/Anomaly Detection (datos históricos de comportamiento y modelo entrenado). Cada servicio es dueño exclusivo de su instancia. |

---

### Redis

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Almacena datos volátiles que requieren velocidad in-memory y expiración automática con TTL nativo. |
| **Uso** | Refresh Tokens activos, blacklist de Access Tokens revocados, contadores de rate limiting distribuido. Exclusivo del Authentication Service. |
| **Justificación** | La verificación de tokens revocados ocurre en cada request — la latencia de disco es inaceptable. TTL nativo expira datos automáticamente sin jobs de mantenimiento. |

---

### Prometheus + Grafana + OpenTelemetry

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Instrumenta, recopila y visualiza métricas y logs operacionales del sistema en tiempo real. |
| **OpenTelemetry** | Instrumenta el código de cada microservicio de forma vendor-neutral, exportando métricas y trazas. |
| **Prometheus** | Almacena métricas de tiempo (latencia de endpoints, intentos de login fallidos, estado de sesiones). |
| **Grafana** | Visualiza dashboards en tiempo real con alertas configurables — un pico de logins fallidos puede indicar un ataque de credential stuffing activo (S-01). |
| **Alcance** | Logs operacionales (errores, debug, latencia) — no confundir con audit logs que son eventos de negocio inmutables gestionados por el Audit Log Service. |

---

### Docker + Docker Compose

| Campo | Detalle |
|---|---|
| **Responsabilidad** | Containeriza cada componente del sistema y orquesta su despliegue local con un solo comando. |
| **Justificación** | Sistema con múltiples servicios heterogéneos — Java, Python, Kafka, PostgreSQL, MongoDB, Redis — que necesitan comunicarse en una red controlada. Docker Compose garantiza entornos reproducibles. |

---

## Mapa de comunicación entre servicios

| Origen | Destino | Tipo | Protocolo | Propósito |
|---|---|---|---|---|
| Cliente | API Gateway | Sincrónico | HTTP/REST | Entrada al sistema |
| API Gateway | Authentication Service | Sincrónico | HTTP/REST | Ruteo de requests |
| Authentication Service | Authorization Service | Sincrónico | HTTP/REST | Validación de identidad antes de evaluar permisos || Authorization Service | ML/Policy Engine (FastAPI) | Sincrónico | HTTP/REST | Evaluación ABAC + score ML en tiempo real |
| Authentication Service | Kafka | Asíncrono | Kafka Producer | Publicación de eventos de autenticación |
| Authorization Service | Kafka | Asíncrono | Kafka Producer | Publicación de decisiones de acceso |
| ML/Policy Engine | Kafka | Asíncrono | Kafka Producer/Consumer | Consumo de eventos para entrenamiento + publicación de evaluaciones |
| Audit Log Service | Kafka | Asíncrono | Kafka Consumer | Consumo de todos los eventos para persistencia inmutable |

---

## Mapa de bases de datos por servicio

| Servicio | PostgreSQL | MongoDB | Redis |
|---|---|---|---|
| Authentication Service | ✅ (usuarios, credenciales, MFA) | — | ✅ (tokens, blacklist, rate limiting) |
| Authorization Service | ✅ (políticas ABAC) | — | — |
| ML / Anomaly Detection + Policy Engine | — | ✅ (comportamiento histórico, modelo ML) | — |
| Audit Log Service | — | ✅ (eventos inmutables, hash chaining) | — |

---

## Trazabilidad con Threat Model

| Amenaza | Severidad | Servicio(s) responsable(s) | Mecanismo de mitigación |
|---|---|---|---|
| E-01: JWT Manipulation | Crítica | Authentication Service | Firma RS256 + validación estricta, rechazo de algoritmo `none` |
| E-02: ABAC Policy Misconfiguration | Alta | Authorization Service + Policy Engine | Policy unit testing, principio de mínimo privilegio, denegación por defecto |
| E-03: Token Reuse After Revocation | Crítica | Authentication Service | Revocación inmediata en Redis + TTL como segunda línea de defensa |
| S-01: Credential Stuffing | Alta | Authentication Service + ML | Rate limiting (Token Bucket) + MFA obligatorio + detección de patrones anómalos |
| S-02: Phishing de tokens | Alta | Authentication Service | CORS estricto + WebAuthn/Passkeys vinculados al dominio legítimo |
| T-01: Audit Log Tampering | Alta | Audit Log Service | Hash chaining — modificación rompe la cadena y es detectable |
| R-01: Negación de acciones administrativas | Media | Audit Log Service | Registro inmutable de identidad, acción, timestamp e IP por evento |
| I-01: Exposición de datos en errores | Media | Todos los servicios | Manejo centralizado de errores — mensajes genéricos al cliente, detalle en logs internos |
| D-01: Authentication Endpoint Flooding | Alta | Authentication Service + ML | Token Bucket distribuido (Redis) + detección de IPs anómalas por ML |

---

## Referencias

- [Spring Security Docs](https://docs.spring.io/spring-security/reference/)
- [FastAPI Docs](https://fastapi.tiangolo.com/)
- [Apache Kafka Docs](https://kafka.apache.org/documentation/)
- [STRIDE Threat Model — Microsoft](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Security Best Practices — IETF RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725)
