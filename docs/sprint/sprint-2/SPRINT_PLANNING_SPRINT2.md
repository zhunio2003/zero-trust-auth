# Sprint Planning — Sprint 2

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Sprint:** Sprint 2  
**Duración:** 1 semana  
**Fecha inicio:** 09/04/2026  
**Fecha fin:** 15/04/2026  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. Sprint Goal

> Implementar el ciclo de vida completo del token en el Authentication Service: registro de usuarios, autenticación con MFA (TOTP + WebAuthn), emisión de JWT firmados con RS256, validación con blacklist, renovación con rotación de Refresh Tokens y revocación inmediata de sesiones.

---

## 2. Capacidad del Sprint

| Concepto | Valor |
|----------|-------|
| Duración del sprint | 7 días |
| Horas disponibles por día | 7h |
| Horas totales disponibles | 49h |
| Factor de productividad (~80%) | 39.2h efectivas |
| Horas estimadas comprometidas | 42.5h |
| Buffer para troubleshooting | -3.3h (sin buffer — sprint agresivo) |

> **Nota sobre la velocidad:** Sprint 1 estableció una línea base de 15 SP completados en 5 días efectivos de 7 disponibles, con estimación de 21.5h que resultó conservadora. Sprint 2 compromete 26 SP — un aumento del 73% — justificado por mayor disponibilidad diaria (7h vs 5h) y la experiencia adquirida con el entorno de desarrollo. La ausencia de buffer es un riesgo aceptado: WebAuthn se difiere a Sprint 3 si el tiempo aprieta.

---

## 3. Contexto del Sprint

Este es el segundo sprint del proyecto ZeroTrust Auth Platform. Corresponde al inicio de la **Fase 2 — Flujo Principal (Core del Sistema)** del Product Backlog, abarcando las primeras 6 historias de la épica **EP-02 — Autenticación**. Este sprint entrega el primer microservicio funcional del sistema: el Authentication Service con el ciclo de vida completo del token (emisión, validación, renovación, revocación) y autenticación con doble factor.

Las historias TS-02.7 (Rate Limiting con Token Bucket) y TS-02.8 (Publicación de eventos a Kafka) quedan para Sprint 3 — complementan el flujo pero no lo rompen si no están presentes. La publicación de eventos se resuelve temporalmente con un EventPublisher mock que será reemplazado en Sprint 3.

**Fase:** Fase 2 — Flujo Principal (Core del Sistema)  
**Épica:** EP-02 — Autenticación

**Dependencias con Sprint 1:**
- Monorepo Gradle con `auth-service` inicializado (TS-01.1) ✅
- Pipeline CI/CD funcional (TS-01.2) ✅
- Docker Compose con PostgreSQL, Redis y redes segmentadas (TS-01.3) ✅
- API Gateway ruteando `/api/auth/**` → auth-service:8081 (TS-01.4) ✅

---

## 4. Historias Comprometidas

| ID | Historia | Story Points | Horas Est. | Tareas |
|----|----------|:------------:|:----------:|:------:|
| US-02.1 | Registro de usuarios | 3 | 6h | 5 |
| US-02.2 | Login con MFA (TOTP + WebAuthn) | 8 | 14h | 6 |
| TS-02.3 | Emisión de tokens JWT (RS256) | 5 | 7.5h | 5 |
| TS-02.4 | Validación de tokens | 5 | 7.5h | 5 |
| TS-02.5 | Renovación de tokens | 3 | 4.5h | 3 |
| US-02.6 | Revocación / cierre de sesión | 2 | 3h | 3 |
| **Total** | | **26 SP** | **42.5h** | **27** |

**Velocidad objetivo:** 26 SP (línea base Sprint 1: 15 SP).

**Historias diferidas a Sprint 3:**

| ID | Historia | Story Points | Justificación |
|----|----------|:------------:|---------------|
| TS-02.7 | Rate limiting con Token Bucket | 8 | Complementa el flujo pero no lo bloquea. Algorítmica compleja que merece sprint con buffer. |
| TS-02.8 | Publicación de eventos a Kafka | 3 | Se resuelve temporalmente con EventPublisher mock en este sprint. |

---

## 5. Descomposición en Tareas por Historia

---

### US-02.1 — Registro de usuarios (3 SP | 6h)

**Story:** "Como desarrollador que integra la API, necesito registrar usuarios con email y contraseña, para que puedan autenticarse en el sistema."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/register` acepta email y contraseña, y retorna HTTP 201 con los datos del usuario creado (sin incluir la contraseña en la respuesta).
2. La contraseña se almacena hasheada en PostgreSQL — nunca en texto plano.
3. Si el email ya está registrado, el endpoint retorna HTTP 409 con un mensaje genérico que no confirma ni niega la existencia del email (prevención de enumeración de usuarios).
4. La contraseña debe cumplir requisitos mínimos de seguridad: longitud mínima de 8 caracteres, al menos una mayúscula, una minúscula y un número. Si no cumple, retorna HTTP 400.
5. El endpoint está documentado en Swagger/OpenAPI.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-02.1.1 | Crear entidad `User` (JPA), repositorio, DTO de request/response y configuración de conexión a PostgreSQL | 1.5h | | |
| T-02.1.2 | Implementar servicio de registro: hasheo BCrypt, validación de contraseña (8+ chars, mayúscula, minúscula, número), verificación de email duplicado | 1.5h | | |
| T-02.1.3 | Implementar controller `POST /api/auth/register` con códigos HTTP correctos (201, 400, 409) y mensajes genéricos | 1h | | |
| T-02.1.4 | Configurar Swagger/OpenAPI y verificar documentación del endpoint | 0.5h | | |
| T-02.1.5 | Tests unitarios: registro exitoso, contraseña débil (400), email duplicado (409), verificar que contraseña no aparece en response | 1.5h | | |

---

### US-02.2 — Login con MFA (8 SP | 14h)

**Story:** "Como usuario registrado, necesito autenticarme con mis credenciales y un segundo factor (TOTP / WebAuthn), para acceder al sistema con doble verificación de identidad."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/login` acepta email y contraseña. Si las credenciales son válidas, el sistema solicita el segundo factor antes de emitir tokens.
2. El flujo MFA soporta TOTP (código temporal de 6 dígitos con ventana de validez de 30 segundos) y WebAuthn/Passkeys (vinculado al dominio legítimo del sistema).
3. Si las credenciales son incorrectas, retorna HTTP 401 con un mensaje genérico ("Credenciales inválidas") sin especificar si el email o la contraseña fue el campo incorrecto.
4. Si el segundo factor es incorrecto, retorna HTTP 401 sin revelar qué método MFA está configurado.
5. Tras autenticación exitosa (credenciales + MFA), el sistema emite Access Token y Refresh Token según la historia TS-02.3.
6. Cada intento de login (exitoso o fallido) genera un evento publicado en Kafka con contexto completo (IP, timestamp, user-agent, resultado).

**Nota sobre dependencias:**
- La emisión de tokens (criterio 5) se conecta cuando se implemente TS-02.3 en este mismo sprint. Hasta entonces, el flujo termina con "MFA verificado exitosamente".
- La publicación de eventos a Kafka (criterio 6) se resuelve con un EventPublisher mock — la interfaz existe pero no publica todavía. Se reemplaza por la implementación real en Sprint 3 (TS-02.8).

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-02.2.1 | Implementar endpoint `POST /api/auth/login` con verificación de credenciales contra PostgreSQL, respuestas genéricas (401) y flujo base que solicita segundo factor | 1.5h | | |
| T-02.2.2 | Implementar flujo TOTP: entidad para secreto TOTP, generación de secreto, endpoint de registro de TOTP (el usuario vincula su app authenticator), validación de código de 6 dígitos con ventana de 30 segundos | 3h | | |
| T-02.2.3 | Implementar flujo WebAuthn: entidades para credenciales WebAuthn, ceremony de registro (challenge → registro de dispositivo → almacenar clave pública), ceremony de autenticación (challenge → verificar firma) | 4h | | |
| T-02.2.4 | Integrar ambos flujos MFA en el endpoint de login: tras credenciales válidas, solicitar segundo factor según método configurado del usuario, sin revelar qué método tiene (401 genérico) | 1.5h | | |
| T-02.2.5 | Crear interfaz EventPublisher con implementación mock (placeholder para Sprint 3 cuando se implemente TS-02.8), publicar eventos en cada intento de login | 1h | | |
| T-02.2.6 | Tests unitarios: login exitoso con TOTP, login exitoso con WebAuthn, credenciales incorrectas (401), código TOTP inválido (401), código TOTP expirado (401), WebAuthn firma inválida (401), verificar mensajes genéricos | 3h | | |

---

### TS-02.3 — Emisión de tokens JWT (5 SP | 7.5h)

**Story:** "Como sistema, necesito generar tokens JWT firmados con RS256 al autenticar exitosamente un usuario, para que las requests posteriores se autentiquen sin consultar la base de datos en cada petición."

**Criterios de aceptación:**

1. El Access Token es un JWT firmado con algoritmo RS256 usando un par de claves asimétricas (pública/privada).
2. El JWT contiene los claims mínimos requeridos: `sub` (identificador del usuario), `role`, `department`, `exp` (expiración) e `iat` (momento de emisión).
3. El Access Token tiene un tiempo de expiración corto (configurable, valor por defecto de 15 minutos).
4. El Refresh Token se genera y almacena en Redis con TTL configurable (valor por defecto de 7 días).
5. Tokens con algoritmo `none` son rechazados explícitamente — el sistema solo acepta RS256.
6. Las claves privadas no están hardcodeadas en el código fuente — se gestionan mediante variables de entorno.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-02.3.1 | Configurar carga de claves RS256: generación del par de claves con script, configuración en variables de entorno, componente Spring que las carga al arrancar | 1.5h | | |
| T-02.3.2 | Implementar generación de Access Token JWT: construir claims (`sub`, `role`, `department`, `exp`, `iat`), firmar con RS256 usando clave privada, expiración configurable (default 15 min) | 1.5h | | |
| T-02.3.3 | Configurar conexión a Redis (Spring Data Redis) e implementar generación y almacenamiento de Refresh Token con TTL configurable (default 7 días) | 1.5h | | |
| T-02.3.4 | Integrar emisión de tokens en el flujo de login: tras MFA exitoso (US-02.2), emitir Access Token + Refresh Token y retornar al cliente | 1h | | |
| T-02.3.5 | Tests unitarios: JWT contiene claims correctos, firma RS256 verificable con clave pública, algoritmo `none` rechazado, Refresh Token existe en Redis con TTL correcto, Access Token expira en tiempo configurado | 2h | | |

---

### TS-02.4 — Validación de tokens (5 SP | 7.5h)

**Story:** "Como sistema, necesito validar la firma y vigencia de cada JWT en cada request, para garantizar que solo tokens legítimos y no revocados permitan el acceso."

**Criterios de aceptación:**

1. La validación verifica la firma RS256 del JWT en memoria usando la clave pública.
2. Tokens con firma inválida, expirados o con claims faltantes (`sub`, `role`, `department`, `exp`, `iat`) son rechazados con HTTP 401.
3. Tokens con algoritmo distinto a RS256 (incluyendo `none`, `HS256`) son rechazados con HTTP 401.
4. El sistema consulta la blacklist de tokens revocados en Redis — si el token está en la blacklist, retorna HTTP 401.
5. Si un Refresh Token ya fue utilizado previamente (token reuse attack), toda la familia de tokens de esa sesión se revoca inmediatamente en Redis y retorna HTTP 401.
6. Los mensajes de error de validación son genéricos ("Token inválido") — no especifican el motivo exacto del rechazo.

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-02.4.1 | Implementar componente de validación JWT: verificar firma RS256 con clave pública, verificar expiración, verificar claims requeridos (`sub`, `role`, `department`, `exp`, `iat`), rechazar algoritmos distintos a RS256 | 2h | | |
| T-02.4.2 | Implementar blacklist en Redis: consultar si el Access Token está en la lista de revocados, rechazar con HTTP 401 si existe | 1h | | |
| T-02.4.3 | Implementar detección de token reuse: al recibir un Refresh Token, verificar en Redis si ya fue consumido. Si fue usado → revocar toda la familia de tokens de esa sesión | 1.5h | | |
| T-02.4.4 | Crear filtro de Spring Security que ejecute la validación automáticamente en cada request protegido, retornando HTTP 401 con mensaje genérico ("Token inválido") en todos los casos de rechazo | 1h | | |
| T-02.4.5 | Tests unitarios: firma válida (pasa), firma inválida (401), token expirado (401), claims faltantes (401), algoritmo `none` (401), algoritmo `HS256` (401), token en blacklist (401), token reuse detectado (401 + familia revocada) | 2h | | |

---

### TS-02.5 — Renovación de tokens (3 SP | 4.5h)

**Story:** "Como sistema, necesito renovar el Access Token usando el Refresh Token, para que los usuarios mantengan su sesión activa sin re-autenticarse mientras el Refresh Token sea válido."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/refresh` acepta un Refresh Token y retorna un nuevo Access Token.
2. El Refresh Token enviado se invalida en Redis tras el uso (rotación de Refresh Tokens) y se emite uno nuevo.
3. Si el Refresh Token no existe en Redis (ya fue usado, expiró o fue revocado), retorna HTTP 401 y revoca toda la familia de tokens de esa sesión (detección de token reuse).
4. El nuevo Access Token se genera con los mismos claims del usuario y un nuevo `exp` e `iat`.
5. El evento de renovación se publica en Kafka con el contexto del request.

**Nota sobre rotación de tokens:** Cada Refresh Token es de un solo uso. Al consumirlo, se destruye y se genera uno nuevo. Si un Refresh Token ya consumido se presenta de nuevo, el sistema detecta token reuse (el token fue robado) y revoca toda la familia — la cadena completa de Refresh Tokens que nacieron de esa sesión queda invalidada. Esto limita el daño de un token comprometido a máximo 15 minutos (vida del Access Token).

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-02.5.1 | Implementar endpoint `POST /api/auth/refresh`: recibir Refresh Token, consultar en Redis si existe. Si no existe → HTTP 401 + revocar toda la familia (reutilizar lógica de token reuse de TS-02.4) | 1.5h | | |
| T-02.5.2 | Implementar rotación: invalidar Refresh Token actual en Redis, emitir nuevo Access Token (mismos claims, nuevo `exp` e `iat`) + nuevo Refresh Token, publicar evento vía EventPublisher mock | 1.5h | | |
| T-02.5.3 | Tests unitarios: refresh exitoso (nuevo Access Token + nuevo Refresh Token), Refresh Token inexistente (401 + familia revocada), Refresh Token expirado (401), verificar que el Refresh Token anterior ya no funciona tras rotación | 1.5h | | |

---

### US-02.6 — Revocación / cierre de sesión (2 SP | 3h)

**Story:** "Como usuario autenticado, necesito cerrar mi sesión para que mis tokens queden invalidados inmediatamente y nadie pueda reutilizarlos."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/logout` acepta el Access Token y el Refresh Token de la sesión activa.
2. El Refresh Token se elimina de Redis inmediatamente.
3. El Access Token se agrega a la blacklist en Redis con TTL igual a su tiempo de expiración restante (no indefinido — expira automáticamente cuando el token original habría expirado).
4. Tras el cierre de sesión, cualquier request con el Access Token o Refresh Token revocado retorna HTTP 401.
5. El evento de cierre de sesión se publica en Kafka con contexto completo (usuario, IP, timestamp).

| ID | Tarea | Horas Est. | Horas Reales | Estado |
|----|-------|:----------:|:------------:|:------:|
| T-02.6.1 | Implementar endpoint `POST /api/auth/logout`: recibir Access Token + Refresh Token, eliminar Refresh Token de Redis, agregar Access Token a blacklist con TTL igual al tiempo de expiración restante, retornar HTTP 200 | 1h | | |
| T-02.6.2 | Publicar evento de cierre de sesión vía EventPublisher mock (usuario, IP, timestamp) | 0.5h | | |
| T-02.6.3 | Tests unitarios: logout exitoso (200), Access Token revocado rechazado en otros endpoints (401), Refresh Token eliminado rechazado en refresh (401), verificar TTL correcto en blacklist | 1.5h | | |

---

## 6. Orden de Ejecución Recomendado

Las historias tienen dependencias lineales estrictas. El orden de ejecución es:

| Orden | Historia | Justificación |
|:-----:|----------|---------------|
| 1 | US-02.1 — Registro de usuarios | Sin usuarios registrados en PostgreSQL no hay credenciales contra las cuales autenticarse. |
| 2 | US-02.2 — Login con MFA | Sin autenticación no hay identidad verificada para emitir tokens. Incluye EventPublisher mock reutilizable por las demás historias. |
| 3 | TS-02.3 — Emisión de tokens JWT | Sin emisión de JWT no hay tokens que validar. Introduce Redis en código por primera vez. Se conecta al flujo de login exitoso de US-02.2. |
| 4 | TS-02.4 — Validación de tokens | Sin validación no hay mecanismo de seguridad por request. Implementa filtro de Spring Security, blacklist y detección de token reuse. |
| 5 | TS-02.5 — Renovación de tokens | Reutiliza emisión (TS-02.3) y token reuse detection (TS-02.4). Sin renovación, las sesiones mueren cada 15 minutos. |
| 6 | US-02.6 — Revocación / cierre de sesión | Reutiliza blacklist (TS-02.4) y operaciones Redis existentes. Cierra el ciclo de vida completo del token. |

---

## 7. Riesgos Identificados

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|:------------:|:-------:|------------|
| WebAuthn consume más tiempo del estimado (4h) por complejidad del protocolo challenge-response con dos ceremonies | Alta | Alta | WebAuthn es la válvula de escape del sprint: si el tiempo aprieta, se implementa solo TOTP en Sprint 2 y se difiere WebAuthn a Sprint 3. El sistema funciona con un solo método MFA. |
| Redis como primera integración en código genera troubleshooting inesperado (conexión, serialización, TTL) | Media | Media | TS-02.3 introduce Redis temprano (historia 3 de 6) — cualquier problema se detecta y resuelve antes de que TS-02.4, TS-02.5 y US-02.6 dependan de él. |
| Sin buffer explícito (-3.3h), cualquier bloqueo impacta directamente la capacidad del sprint | Media | Alta | Las dos últimas historias (TS-02.5 y US-02.6) reutilizan piezas ya implementadas — su tiempo real será probablemente menor al estimado, generando buffer implícito. |
| Pipeline CI/CD actualmente en rojo (esperado desde Sprint 1) — Sprint 2 debe hacer pasar linting y tests reales | Media | Baja | Checkstyle y tests se ejecutan localmente antes de cada push. Los errores de pipeline se corrigen en la primera historia. |
| Curva de aprendizaje de criptografía (RS256, TOTP, WebAuthn) sin experiencia previa | Alta | Media | Action item de Sprint 1 Retrospective: estudiar el concepto y modelo mental básico de cada tecnología (20-30 min) antes de implementarla. Las horas por tarea ya incluyen tiempo de estudio. |

---

## 8. Definition of Done del Sprint

Al finalizar el sprint, el incremento debe cumplir:

1. Todas las historias cumplen el DoD de Historia (Nivel 1) — incluyendo los puntos de seguridad aplicables: JWT firmado con RS256, rechazo de algoritmo `none`, claims mínimos presentes, mensajes de error genéricos, credenciales no expuestas en logs.
2. El Authentication Service está desplegado y funcionando como contenedor Docker en la red `services` con conexión a PostgreSQL (red `data`) y Redis (red `data`).
3. El ciclo de vida completo del token funciona de extremo a extremo: registro → login con MFA → emisión de JWT → validación por request → renovación con rotación → revocación con blacklist.
4. El pipeline de GitHub Actions ejecuta Checkstyle y JUnit sobre auth-service exitosamente.
5. Todos los negative security tests pasan: JWT con algoritmo `none` rechazado (401), JWT con firma inválida rechazado (401), JWT con claims faltantes rechazado (401), token en blacklist rechazado (401), token reuse detectado y familia revocada (401).

---

## 9. Resumen

| Concepto | Valor |
|----------|-------|
| Sprint | Sprint 2 |
| Duración | 1 semana (09/04/2026 — 15/04/2026) |
| Sprint Goal | Ciclo de vida completo del token en Authentication Service |
| Épica | EP-02 — Autenticación |
| Historias comprometidas | 6 de 8 |
| Historias diferidas a Sprint 3 | 2 (TS-02.7 Rate Limiting, TS-02.8 Kafka Producer) |
| Story Points comprometidos | 26 SP |
| Velocidad Sprint 1 (línea base) | 15 SP |
| Aumento de velocidad | +73% |
| Horas estimadas | 42.5h |
| Horas efectivas disponibles | 39.2h |
| Buffer disponible | -3.3h (sin buffer — sprint agresivo) |
| Total de tareas | 27 |

---

## Referencias

- [Scrum Guide — Sprint Planning](https://scrumguides.org/scrum-guide.html#sprint-planning)
- [Product Backlog — ZeroTrust Auth Platform](../project/PRODUCT_BACKLOG_ZEROTRUST.md)
- [Definition of Done — ZeroTrust Auth Platform](../project/DEFINITION_OF_DONE_ZEROTRUST.md)
- [Sprint Review Sprint 1](../product/SPRINT_REVIEW_SPRINT1.md)
- [Sprint Retrospective Sprint 1](../product/SPRINT_RETROSPECTIVE_SPRINT1.md)
- [JWT Security Best Practices — IETF RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725)
- [WebAuthn Specification — W3C](https://www.w3.org/TR/webauthn-3/)
- [TOTP Algorithm — IETF RFC 6238](https://datatracker.ietf.org/doc/html/rfc6238)
