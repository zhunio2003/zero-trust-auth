# Product Backlog — Épicas e Historias

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Versión:** 1.0  
**Fecha:** 2026-04-01  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. Introducción

Este documento define las **épicas** e **historias** del Product Backlog del sistema ZeroTrust Auth Platform, organizadas en fases de desarrollo priorizadas. Las épicas fueron derivadas a partir del análisis de los perfiles de usuario definidos en el Product Vision Board, los microservicios establecidos en la Arquitectura Detallada y las amenazas identificadas en el Threat Model STRIDE. Las historias descomponen cada épica en unidades de trabajo entregables por sprint.

**Formato de las historias:**

- **User Stories:** "Como [tipo de usuario], necesito [acción], para [beneficio]."
- **Technical Stories:** "Como [sistema / equipo de desarrollo], necesito [capacidad técnica], para que [beneficio técnico o habilitación]."

**Documentos de referencia:**

- `docs/zerotrust-auth-platform.md` — Product Vision Board con 3 perfiles de usuario (Tech Lead, Backend Developer, System Administrator).
- `docs/DETAILED_ARCHITECTURE.md` — Arquitectura detallada con 4 microservicios y principios arquitectónicos.
- `docs/COMPONENT_DIAGRAM.md` — Diagrama de componentes por servicio.
- `docs/DEPLOYMENT_DIAGRAM.md` — Diagrama de despliegue con redes Docker segmentadas.
- `docs/THREAT-MODEL_STRIDE.md` — Modelo de amenazas STRIDE con 9 amenazas identificadas.
- `docs/TECHNOLOGY_STACK_ZEROTRUST.md` — Decisiones tecnológicas del proyecto.
- `docs/DEFINITION_OF_DONE_ZEROTRUST.md` — Definition of Done con 31 puntos de verificación en 3 niveles.

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

## 5. Historias por Épica

---

### Fase 1 — Fundación (Infraestructura)

---

### EP-01 — Infraestructura / Setup del Proyecto

Todas las historias de esta épica son **Technical Stories** — no tienen usuario directo pero son prerequisito para que cualquier servicio exista.

---

#### TS-01.1 — Estructura del repositorio

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

---

#### TS-01.2 — Pipeline CI/CD

**Story:** "Como equipo de desarrollo, necesitamos un pipeline de integración continua, para que cada push al repositorio ejecute automáticamente linting, pruebas y build sin intervención manual."

**Criterios de aceptación:**

1. El pipeline de GitHub Actions se activa automáticamente en cada push al repositorio.
2. El pipeline ejecuta linting con Checkstyle para servicios Java (auth-service, authz-service, audit-log-service) y Flake8 para el servicio Python (ml-policy-engine).
3. El pipeline ejecuta pruebas unitarias con JUnit para servicios Java y Pytest para el servicio Python.
4. El pipeline construye las imágenes Docker de los servicios modificados.
5. Si cualquier paso falla (linting, pruebas o build), el pipeline se detiene y reporta el error.
6. El pipeline se ejecuta exitosamente sobre la estructura base del repositorio sin errores.

---

#### TS-01.3 — Docker Compose con redes segmentadas

**Story:** "Como equipo de desarrollo, necesitamos un Docker Compose que levante toda la infraestructura del sistema con un solo comando, para que los microservicios puedan desarrollarse y probarse en un entorno reproducible con redes aisladas."

**Criterios de aceptación:**

1. Las 4 redes Docker están definidas: `public`, `services`, `data`, `monitoring`.
2. Los contenedores de infraestructura base están configurados: PostgreSQL x2 (authn, authz), MongoDB x2 (audit, ml), Redis, Kafka.
3. Los volúmenes de persistencia están definidos para cada base de datos, Kafka, Prometheus y Grafana según el Deployment Diagram.
4. Solo `frontend` (:80) y `api-gateway` (:8080) exponen puertos al host. El resto opera exclusivamente en redes internas.
5. Cada contenedor está asignado a las redes correctas según el Deployment Diagram: los microservicios pertenecen a `services`, `data` y `monitoring`; las bases de datos solo a `data`; Kafka solo a `services`.
6. El comando `docker compose up` levanta todos los contenedores sin errores y los healthchecks reportan estado saludable.
7. Los contenedores de Prometheus y Grafana están configurados en la red `monitoring`.

---

#### TS-01.4 — API Gateway mínimo

**Story:** "Como sistema, necesito un API Gateway que rutee los requests entrantes al microservicio correspondiente, para que los servicios internos no estén expuestos directamente al exterior."

**Criterios de aceptación:**

1. El API Gateway está implementado con Spring Boot y desplegado como contenedor Docker en las redes `public` y `services`.
2. El Gateway rutea requests a los microservicios internos por ruta: `/api/auth/**` → auth-service, `/api/authz/**` → authz-service, `/api/audit/**` → audit-log-service.
3. El Gateway expone un endpoint `/health` que retorna HTTP 200 confirmando que el servicio está operativo.
4. Los requests a rutas no definidas retornan HTTP 404 con un mensaje genérico (sin exponer detalles internos del sistema).
5. El Gateway es accesible en el puerto 8080 del host y rutea correctamente a los servicios internos dentro de la red `services`.

---

### Fase 2 — Flujo Principal (Core del Sistema)

---

### EP-02 — Autenticación

Historias del **Authentication Service** (Java + Spring Boot). Bases de datos: PostgreSQL (usuarios, credenciales, MFA) y Redis (tokens, blacklist, rate limiting).

---

#### US-02.1 — Registro de usuarios

**Story:** "Como desarrollador que integra la API, necesito registrar usuarios con email y contraseña, para que puedan autenticarse en el sistema."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/register` acepta email y contraseña, y retorna HTTP 201 con los datos del usuario creado (sin incluir la contraseña en la respuesta).
2. La contraseña se almacena hasheada en PostgreSQL — nunca en texto plano.
3. Si el email ya está registrado, el endpoint retorna HTTP 409 con un mensaje genérico que no confirma ni niega la existencia del email (prevención de enumeración de usuarios).
4. La contraseña debe cumplir requisitos mínimos de seguridad: longitud mínima de 8 caracteres, al menos una mayúscula, una minúscula y un número. Si no cumple, retorna HTTP 400.
5. El endpoint está documentado en Swagger/OpenAPI.

---

#### US-02.2 — Inicio de sesión con MFA

**Story:** "Como usuario registrado, necesito autenticarme con mis credenciales y un segundo factor (TOTP / WebAuthn), para acceder al sistema con doble verificación de identidad."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/login` acepta email y contraseña. Si las credenciales son válidas, el sistema solicita el segundo factor antes de emitir tokens.
2. El flujo MFA soporta TOTP (código temporal de 6 dígitos con ventana de validez de 30 segundos) y WebAuthn/Passkeys (vinculado al dominio legítimo del sistema).
3. Si las credenciales son incorrectas, retorna HTTP 401 con un mensaje genérico ("Credenciales inválidas") sin especificar si el email o la contraseña fue el campo incorrecto.
4. Si el segundo factor es incorrecto, retorna HTTP 401 sin revelar qué método MFA está configurado.
5. Tras autenticación exitosa (credenciales + MFA), el sistema emite Access Token y Refresh Token según la historia TS-02.3.
6. Cada intento de login (exitoso o fallido) genera un evento publicado en Kafka con contexto completo (IP, timestamp, user-agent, resultado).

---

#### TS-02.3 — Emisión de tokens JWT

**Story:** "Como sistema, necesito generar tokens JWT firmados con RS256 al autenticar exitosamente un usuario, para que las requests posteriores se autentiquen sin consultar la base de datos en cada petición."

**Criterios de aceptación:**

1. El Access Token es un JWT firmado con algoritmo RS256 usando un par de claves asimétricas (pública/privada).
2. El JWT contiene los claims mínimos requeridos: `sub` (identificador del usuario), `role`, `department`, `exp` (expiración) e `iat` (momento de emisión).
3. El Access Token tiene un tiempo de expiración corto (configurable, valor por defecto de 15 minutos).
4. El Refresh Token se genera y almacena en Redis con TTL configurable (valor por defecto de 7 días).
5. Tokens con algoritmo `none` son rechazados explícitamente — el sistema solo acepta RS256.
6. Las claves privadas no están hardcodeadas en el código fuente — se gestionan mediante variables de entorno.

---

#### TS-02.4 — Validación de tokens

**Story:** "Como sistema, necesito validar la firma y vigencia de cada JWT en cada request, para garantizar que solo tokens legítimos y no revocados permitan el acceso."

**Criterios de aceptación:**

1. La validación verifica la firma RS256 del JWT en memoria usando la clave pública.
2. Tokens con firma inválida, expirados o con claims faltantes (`sub`, `role`, `department`, `exp`, `iat`) son rechazados con HTTP 401.
3. Tokens con algoritmo distinto a RS256 (incluyendo `none`, `HS256`) son rechazados con HTTP 401.
4. El sistema consulta la blacklist de tokens revocados en Redis — si el token está en la blacklist, retorna HTTP 401.
5. Si un Refresh Token ya fue utilizado previamente (token reuse attack), toda la familia de tokens de esa sesión se revoca inmediatamente en Redis y retorna HTTP 401.
6. Los mensajes de error de validación son genéricos ("Token inválido") — no especifican el motivo exacto del rechazo.

---

#### TS-02.5 — Renovación de tokens

**Story:** "Como sistema, necesito renovar el Access Token usando el Refresh Token, para que los usuarios mantengan su sesión activa sin re-autenticarse mientras el Refresh Token sea válido."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/refresh` acepta un Refresh Token y retorna un nuevo Access Token.
2. El Refresh Token enviado se invalida en Redis tras el uso (rotación de Refresh Tokens) y se emite uno nuevo.
3. Si el Refresh Token no existe en Redis (ya fue usado, expiró o fue revocado), retorna HTTP 401 y revoca toda la familia de tokens de esa sesión (detección de token reuse).
4. El nuevo Access Token se genera con los mismos claims del usuario y un nuevo `exp` e `iat`.
5. El evento de renovación se publica en Kafka con el contexto del request.

---

#### US-02.6 — Revocación / cierre de sesión

**Story:** "Como usuario autenticado, necesito cerrar mi sesión para que mis tokens queden invalidados inmediatamente y nadie pueda reutilizarlos."

**Criterios de aceptación:**

1. El endpoint `POST /api/auth/logout` acepta el Access Token y el Refresh Token de la sesión activa.
2. El Refresh Token se elimina de Redis inmediatamente.
3. El Access Token se agrega a la blacklist en Redis con TTL igual a su tiempo de expiración restante (no indefinido — expira automáticamente cuando el token original habría expirado).
4. Tras el cierre de sesión, cualquier request con el Access Token o Refresh Token revocado retorna HTTP 401.
5. El evento de cierre de sesión se publica en Kafka con contexto completo (usuario, IP, timestamp).

---

#### TS-02.7 — Rate limiting con Token Bucket

**Story:** "Como sistema, necesito limitar la cantidad de requests al endpoint de autenticación por IP, para mitigar ataques de credential stuffing y flooding que intentan saturar el servicio."

**Criterios de aceptación:**

1. El algoritmo Token Bucket está implementado desde cero (no usando una librería externa de rate limiting).
2. Los contadores del bucket se almacenan en Redis, permitiendo que múltiples instancias del Authentication Service compartan el mismo estado.
3. Cada IP tiene su propio bucket con capacidad y tasa de recarga configurables mediante variables de entorno.
4. Cuando el bucket de una IP está vacío, el request se rechaza con HTTP 429 (Too Many Requests) incluyendo el header `Retry-After` con el tiempo de espera.
5. El rate limiting es el primer filtro del request — se evalúa antes de verificar credenciales o tokens.
6. Cada evento de rate limiting (request rechazado por límite excedido) se publica en Kafka para consumo por el ML/Anomaly Detection.

---

#### TS-02.8 — Publicación de eventos a Kafka

**Story:** "Como sistema, necesito publicar eventos de autenticación en Kafka, para que el Audit Log Service y el ML/Anomaly Detection puedan consumirlos de forma asíncrona sin bloquear el flujo principal."

**Criterios de aceptación:**

1. Cada operación del Authentication Service genera un evento publicado en Kafka: login exitoso, login fallido, registro, emisión de token, renovación de token, revocación de sesión, rate limiting activado.
2. Cada evento contiene el contexto completo del request: IP, timestamp, user-agent, usuario (si aplica) y resultado de la operación.
3. Los eventos se publican en un topic de Kafka dedicado al Authentication Service.
4. La publicación es asíncrona — no bloquea la respuesta al cliente. Si Kafka no está disponible temporalmente, el servicio sigue respondiendo requests (degradación graceful).
5. El formato del evento es JSON con un esquema consistente que incluye `event_type`, `timestamp`, `user_id`, `ip`, `user_agent` y `result`.

---

### EP-03 — Autorización

Historias del **Authorization Service** (Java + Spring Boot). Base de datos: PostgreSQL propia (políticas ABAC). La llamada al Policy Engine se mockea con Mockito hasta que el EP-04 esté implementado.

---

#### TS-03.1 — Extracción de atributos del JWT

**Story:** "Como sistema, necesito extraer los atributos del usuario directamente del JWT, para evaluar la autorización sin acceder a la base de datos del Authentication Service, respetando el principio de database per service."

**Criterios de aceptación:**

1. El Authorization Service lee los claims `sub`, `role` y `department` directamente del JWT recibido en el request.
2. Si algún claim requerido está ausente o tiene formato inválido, el request se rechaza con HTTP 401.
3. El servicio no realiza ninguna consulta a la base de datos del Authentication Service — los atributos del usuario viajan exclusivamente dentro del JWT.
4. Los atributos extraídos se pasan como parámetros a la evaluación de políticas ABAC (TS-03.2 y TS-03.3).

---

#### TS-03.2 — Consulta de políticas ABAC

**Story:** "Como sistema, necesito leer las políticas de autorización desde la base de datos, para evaluar si los atributos del usuario cumplen las condiciones requeridas para acceder a cada recurso."

**Criterios de aceptación:**

1. Las políticas ABAC se almacenan en la instancia PostgreSQL propia del Authorization Service (no en la del Authentication Service).
2. Cada política contiene: recurso protegido, condiciones (atributo, operador, valor) y acción (permitir/denegar).
3. Las políticas soportan los atributos del JWT (`role`, `department`) y atributos de contexto (`hora`, `IP`, `recurso solicitado`).
4. Las políticas soportan el score de confianza del ML como atributo evaluable (por ejemplo: "denegar si score < 0.5").
5. El servicio carga las políticas de forma eficiente sin consultar la base de datos en cada request (caché en memoria con invalidación configurable).
6. Si no existe una política que cubra el recurso solicitado, el acceso se deniega por defecto (principio de mínimo privilegio).

---

#### TS-03.3 — Evaluación de acceso

**Story:** "Como sistema, necesito evaluar si un usuario autenticado tiene permiso para acceder a un recurso específico, delegando la decisión final al Policy Engine que combina las políticas ABAC con el score de anomalías del ML."

**Criterios de aceptación:**

1. El Authorization Service envía una llamada sincrónica HTTP al Policy Engine (Python/FastAPI) con los atributos del JWT, el contexto del request y las políticas aplicables.
2. El Policy Engine retorna la decisión de acceso: `access_granted` o `access_denied` con el motivo.
3. Si el Policy Engine no está disponible, el acceso se deniega por defecto (fail-closed, no fail-open).
4. Inicialmente, la llamada al Policy Engine se mockea con Mockito para permitir el desarrollo y testing del Authorization Service antes de que el EP-04 esté implementado.
5. Todas las combinaciones de condiciones ABAC están cubiertas por policy unit tests que verifican edge cases, incluyendo el uso correcto de operadores AND/OR.
6. La respuesta al cliente es HTTP 200 (acceso permitido) o HTTP 403 (acceso denegado) con un mensaje genérico.

---

#### TS-03.4 — Publicación de eventos a Kafka

**Story:** "Como sistema, necesito publicar las decisiones de acceso en Kafka, para que el Audit Log Service registre cada autorización otorgada o denegada con trazabilidad completa."

**Criterios de aceptación:**

1. Cada decisión de acceso genera un evento publicado en Kafka: `access_granted` o `access_denied`.
2. Cada evento contiene: usuario, recurso solicitado, atributos evaluados, políticas aplicadas, decisión y motivo.
3. Los eventos se publican en un topic de Kafka dedicado al Authorization Service.
4. La publicación es asíncrona y no bloquea la respuesta al cliente.
5. El formato del evento es JSON consistente con el esquema definido en TS-02.8 (misma estructura base con campos adicionales propios de autorización).

---

### EP-04 — Detección de Anomalías

Historias del **ML / Anomaly Detection + Policy Engine** (Python + FastAPI). Base de datos: MongoDB propia (datos históricos de comportamiento y modelo ML entrenado). Dos responsabilidades co-localizadas en el mismo proceso por cohesión técnica.

---

#### TS-04.1 — Consumo de eventos de Kafka

**Story:** "Como sistema, necesito consumir los eventos de autenticación desde Kafka, para alimentar el modelo ML con datos de comportamiento real de los usuarios."

**Criterios de aceptación:**

1. El servicio consume eventos del topic de Kafka del Authentication Service de forma asíncrona.
2. Los datos de comportamiento extraídos de cada evento (IP, horario, user-agent, tipo de operación) se persisten en MongoDB como historial por usuario.
3. Si el servicio estuvo caído, al reconectarse procesa los mensajes retenidos por Kafka sin pérdida de datos.
4. Los eventos con formato inválido o campos faltantes se descartan con un log de error — no interrumpen el procesamiento de otros eventos.
5. El consumo de eventos no afecta el rendimiento del flujo sincrónico del Policy Engine (son procesos independientes dentro del mismo servicio).

---

#### TS-04.2 — Entrenamiento y actualización del modelo ML

**Story:** "Como sistema, necesito entrenar un modelo de detección de anomalías con los datos históricos de comportamiento, para que el sistema aprenda qué es normal para cada usuario y detecte desviaciones."

**Criterios de aceptación:**

1. El modelo aprende patrones normales de comportamiento por usuario: IPs frecuentes, horarios habituales, dispositivos conocidos, velocidad típica de requests.
2. El entrenamiento utiliza los datos históricos almacenados en MongoDB por la historia TS-04.1.
3. El modelo entrenado se persiste en MongoDB para sobrevivir reinicios del servicio — no se pierde el aprendizaje.
4. El modelo se actualiza periódicamente con nuevos datos sin necesidad de reentrenamiento completo desde cero.
5. El servicio funciona con un modelo por defecto cuando no hay suficientes datos históricos de un usuario (nuevo usuario, primeras interacciones).

---

#### TS-04.3 — Cálculo de score de confianza

**Story:** "Como sistema, necesito calcular un score de confianza por cada request en tiempo real, para que el Policy Engine pueda incorporar el nivel de riesgo del comportamiento actual en la decisión de acceso."

**Criterios de aceptación:**

1. El score se calcula comparando el request actual contra el patrón histórico del usuario: IP, horario, velocidad de requests y user-agent.
2. El score es un valor numérico normalizado entre 0 (completamente anómalo) y 1 (completamente normal).
3. El cálculo se ejecuta en el mismo proceso Python/FastAPI que el Policy Engine — sin llamadas de red.
4. El cálculo responde en menos de 50ms para no impactar la latencia del flujo sincrónico de autorización.
5. Si no hay datos históricos suficientes para el usuario, el score retorna un valor por defecto configurable (ni máximo ni mínimo — un valor neutro que no bloquee ni otorgue confianza total).

---

#### TS-04.4 — Evaluación de políticas ABAC (Policy Engine)

**Story:** "Como sistema, necesito evaluar las políticas ABAC combinando los atributos del usuario, el contexto del request y el score de anomalías del ML, para emitir la decisión final de acceso que el Authorization Service espera."

**Criterios de aceptación:**

1. El Policy Engine expone un endpoint HTTP que recibe los atributos del JWT, el contexto del request y las políticas ABAC del Authorization Service.
2. El Policy Engine consume el score de confianza del módulo ML (TS-04.3) directamente en el mismo proceso sin llamada de red.
3. La evaluación combina todos los atributos: `role`, `department`, `hora`, `IP`, `recurso` y `score de confianza` contra las condiciones de las políticas.
4. Si el score de confianza está por debajo del umbral definido en la política, el acceso se deniega independientemente de que los demás atributos sean válidos.
5. La respuesta es `access_granted` o `access_denied` con el motivo detallado (para registro en audit log, no para el cliente final).
6. Esta historia reemplaza el mock de Mockito utilizado en TS-03.3 — tras su implementación, el Authorization Service se conecta al Policy Engine real.
7. Denegación por defecto ante cualquier caso no cubierto por las políticas.

---

#### TS-04.5 — Publicación de eventos a Kafka

**Story:** "Como sistema, necesito publicar el resultado de cada evaluación en Kafka, para que el Audit Log Service registre el score, los atributos evaluados y la decisión emitida con trazabilidad completa."

**Criterios de aceptación:**

1. Cada evaluación del Policy Engine genera un evento publicado en Kafka: score calculado, atributos evaluados, decisión emitida y motivo.
2. Los eventos se publican en un topic de Kafka dedicado al ML/Policy Engine.
3. La publicación es asíncrona y no bloquea la respuesta sincrónica al Authorization Service.
4. El formato del evento es JSON consistente con el esquema base definido en TS-02.8.

---

### Fase 3 — Trazabilidad y Administración

> **Nota:** Las historias de esta fase se presentan con nombre y story únicamente. Los criterios de aceptación se detallarán cuando se acerquen al sprint correspondiente (refinamiento progresivo).

---

### EP-05 — Auditoría

Historias del **Audit Log Service** (Java + Spring Boot). Base de datos: MongoDB propia (eventos inmutables con hash chaining).

---

#### TS-05.1 — Consumo de eventos de Kafka

**Story:** "Como sistema, necesito consumir eventos de todos los servicios productores desde Kafka, para centralizar la auditoría de todas las operaciones del sistema en un solo punto."

---

#### TS-05.2 — Persistencia inmutable con hash chaining

**Story:** "Como sistema, necesito almacenar cada evento con hash chaining inmutable, para que cualquier modificación o eliminación sea detectable y los audit logs tengan valor forense."

---

#### TS-05.3 — API de consulta con filtros

**Story:** "Como sistema, necesito exponer endpoints de consulta de audit logs con filtros, para que el Admin Dashboard pueda buscar eventos por usuario, fecha, tipo de acción y severidad."

---

#### TS-05.4 — Verificación de integridad de la cadena

**Story:** "Como sistema, necesito verificar la integridad de la cadena de hashes recalculando secuencialmente todos los eventos, para confirmar que ningún registro fue alterado."

---

#### TS-05.5 — Publicación de eventos a Kafka

**Story:** "Como sistema, necesito publicar los eventos propios del servicio de auditoría en Kafka, para registrar verificaciones de integridad ejecutadas y errores de consumo detectados."

---

### EP-06 — Admin Dashboard

Historias del **Admin Dashboard** (React). Interfaz web mínima y funcional que consume las APIs REST de los microservicios backend.

---

#### US-06.1 — Gestión de usuarios

**Story:** "Como administrador del sistema, necesito visualizar la lista de usuarios registrados y gestionar sus cuentas (activar, desactivar, eliminar), para mantener el control de acceso a la plataforma."

---

#### US-06.2 — Monitoreo de sesiones activas

**Story:** "Como administrador del sistema, necesito visualizar las sesiones activas en tiempo real (usuario, IP, dispositivo, duración) y poder revocar sesiones sospechosas, para responder ante posibles compromisos de seguridad."

---

#### US-06.3 — Visualización de audit logs

**Story:** "Como administrador del sistema, necesito consultar los eventos de auditoría con filtros (por usuario, fecha, tipo de acción, severidad), para investigar incidentes de seguridad y cumplir con requisitos de auditoría."

---

#### US-06.4 — Verificación de integridad

**Story:** "Como administrador del sistema, necesito ejecutar la verificación de integridad de la cadena de hashes desde el dashboard y visualizar el resultado, para confirmar que ningún evento de auditoría fue alterado."

---

#### US-06.5 — Gestión de políticas ABAC

**Story:** "Como administrador del sistema, necesito crear, editar y eliminar políticas de autorización ABAC desde el dashboard, para gestionar los permisos de acceso sin modificar código."

---

#### US-06.6 — Visualización de amenazas y alertas

**Story:** "Como administrador del sistema, necesito visualizar las alertas generadas por el motor de detección de anomalías (IPs sospechosas, horarios inusuales, patrones anómalos), para tomar acciones preventivas en tiempo real."

---

## 6. Trazabilidad con Threat Model

| Épica | Amenazas que mitiga | Historias clave |
|-------|---------------------|-----------------|
| EP-01 | — (infraestructura base) | — |
| EP-02 | E-01 (JWT Manipulation), E-03 (Token Reuse After Revocation), S-01 (Credential Stuffing), S-02 (Phishing de tokens), D-01 (Authentication Endpoint Flooding) | TS-02.3, TS-02.4, TS-02.5, US-02.2, TS-02.7 |
| EP-03 | E-02 (ABAC Policy Misconfiguration) | TS-03.2, TS-03.3 |
| EP-04 | S-01 (detección de patrones anómalos), D-01 (detección de IPs anómalas), E-02 (evaluación ABAC con score ML) | TS-04.2, TS-04.3, TS-04.4 |
| EP-05 | T-01 (Audit Log Tampering), R-01 (Negación de acciones administrativas) | TS-05.2, TS-05.4 |
| EP-06 | — (interfaz de gestión, no mitiga amenazas directamente) | — |

> La amenaza I-01 (Exposición de datos sensibles en errores) se mitiga de forma transversal en todos los servicios mediante manejo centralizado de errores — no pertenece a una épica ni historia específica. Se verifica en el DoD de Historia (Nivel 1, puntos 20 y 21).

---

## 7. Resumen

| Concepto | Valor |
|----------|-------|
| Total de épicas | 6 |
| Total de historias | 33 |
| Technical Stories | 25 |
| User Stories | 8 |
| Historias con criterios de aceptación (Fase 1 + Fase 2) | 22 |
| Historias sin criterios — refinamiento progresivo (Fase 3) | 11 |
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
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Security Best Practices — IETF RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725)
