# Threat Model — ZeroTrust Auth Platform
**Framework:** STRIDE  
**Versión:** 1.0  
**Fecha:** 2026-03-25  
**Autor:** Miguel Angel Zhunio Remache

---

## Contexto del sistema

Una plataforma de autenticación y autorización de nivel empresarial, construida desde cero con arquitectura de microservicios. Implementa el modelo **Zero Trust** real: ninguna solicitud es confiable por defecto, cada acceso se verifica continuamente en función de identidad, contexto y comportamiento.


---

## Assets críticos

| Asset | Descripción | Sensibilidad |
|---|---|---|
| Access Token (JWT) | Token de acceso emitido por el sistema | Alta|
| Refresh Token | Token de larga duración para renovar sesiones | Alta |
| Credenciales de usuario | Usuario y contraseña almacenados en el sistema | Alta |
| Audit logs | Registro inmutable de acciones del sistema | Alta |
| Políticas ABAC | Reglas de autorización del motor de acceso | Alta |
| Disponibilidad del servicio | Capacidad del sistema de autenticar usuarios | Alta |


---

## Amenazas identificadas

---

### E — Elevation of Privilege

#### E-01: JWT Manipulation

| Campo | Detalle |
|---|---|
| **Asset afectado** | ACCESS TOKEN (JWT)|
| **Descripción de la amenaza** | Un atacante intercepta un JWT, lo modifica para elevar su rol a administrador y obtener acceso no autorizado al sistema.|
| **Vector de ataque** | La ausencia de firma o la mala configuración en la validación de las mismas podría llevar al servidor a aceptar JWTs inválidos o manipulados. |
| **Impacto si tiene éxito** | El acceso total al dashboard del administrador permite al atacante visualizar datos privados de usuarios, gestionar cuentas de forma arbitraria y emitir tokens fraudulentos permitiendo suplantación de identidad a cualquier nivel del sistema. |
| **Mitigación** | Los JWTs deben firmarse con RS256 y la validación debe ser estricta, rechazando explícitamente tokens con algoritmo none o firmas inválidas sin excepciones ni fallbacks. |
| **Severidad** | Crítica  |

---

#### E-02: ABAC Policy Misconfiguration

| Campo | Detalle |
|---|---|
| **Asset afectado** | Recursos protegidos de la API (endpoints restringidos) |
| **Descripción de la amenaza** | Un atacante explota errores lógicos en las políticas ABAC enviando combinaciones de parámetros que el sistema evalúa como válidas, obteniendo acceso no autorizado a recursos privados. |
| **Vector de ataque** | Políticas ABAC mal definidas con errores lógicos en sus condiciones, incluyendo el uso incorrecto de operadores AND/OR, generando edge cases no cubiertos que el sistema evalúa como acceso válido. |
| **Impacto si tiene éxito** | Un atacante obtiene acceso no autorizado al panel administrativo, pudiendo visualizar datos privados de usuarios, sesiones activas y logs de auditoría sin autorización legítima. |
| **Mitigación** | Implementar policy unit testing que cubra edge cases y combinaciones límite, aplicando principio de mínimo privilegio con denegación por defecto en todas las políticas ABAC. |
| **Severidad** | Alta |

---

#### E-03: Token Reuse After Revocation

| Campo | Detalle |
|---|---|
| **Asset afectado** | Refresh Token|
| **Descripción de la amenaza** | Un atacante que ha obtenido un refresh token lo reutiliza para generar access tokens válidos incluso después de que la sesión fue revocada, manteniéndose activo en el sistema sin autorización. |
| **Vector de ataque** | El sistema no invalida el refresh token en Redis al momento de la revocación de sesión, permitiendo que tokens comprometidos sigan siendo funcionales indefinidamente. |
| **Impacto si tiene éxito** | El atacante suplanta la identidad del usuario legítimo manteniendo acceso persistente a su cuenta y datos personales, incluso después de que la víctima cerró sesión creyendo estar protegida. |
| **Mitigación** | Implementar revocación inmediata del refresh token en Redis al cerrar sesión, combinado con TTL (Time To Live) que garantiza expiración automática como segunda línea de defensa. |
| **Severidad** | Crítica |

---

### S — Spoofing

#### S-01: Credential Stuffing

| Campo | Detalle |
|---|---|
| **Asset afectado** | Credenciales de usuario |
| **Descripción de la amenaza** | El atacante toma listas de usuario/contraseña filtradas de otras plataformas y las prueba masivamente contra este sistema. Como la gente reutiliza contraseñas, estadísticamente un porcentaje funciona. |
| **Vector de ataque** | Ausencia de rate limiting en el endpoint de login y falta de MFA obligatorio |
| **Impacto si tiene éxito** | Acceso no autorizado a cuentas legítimas de usuarios |
| **Mitigación** | Rate limiting en endpoint de autenticación + MFA obligatorio |
| **Severidad** | Alta  |

#### S-02: Phishing de tokens

| Campo | Detalle |
|---|---|
| **Asset afectado** | Access Token / credenciales de usuario |
| **Descripción de la amenaza** | El atacante replica la interfaz de login para robar credenciales o tokens directamente del usuario. |
| **Vector de ataque** | Ausencia de validación de origen de las solicitudes (CORS mal configurado o ausente) |
| **Impacto si tiene éxito** | Robo de credenciales o tokens válidos permitiendo acceso total a la cuenta |
| **Mitigación** | Implementar CORS estricto + WebAuthn/Passkeys que vinculan la autenticación al dominio legítimo |
| **Severidad** | Alta  |

---

### T — Tampering

#### T-01: Audit Log Tampering

| Campo | Detalle |
|---|---|
| **Asset afectado** | Audit logs |
| **Descripción de la amenaza** | Este sistema tiene audit logs con hash chaining — cada evento firma al anterior. La amenaza es que un atacante con acceso al sistema intente modificar o eliminar logs para borrar evidencia de un ataque previo. |
| **Vector de ataque** | Acceso directo a la base de datos de logs sin pasar por la API, o permisos mal configurados sobre MongoDB |
| **Impacto si tiene éxito** | Eliminación de evidencia de ataques previos, imposibilidad de auditar incidentes de seguridad |
| **Mitigación** | Hash chaining en cada evento — cualquier modificación rompe la cadena y es detectable. Acceso a logs solo mediante API con autenticación |
| **Severidad** | Alta |

---

### R — Repudiation

#### R-01: Negación de acciones administrativas

| Campo | Detalle |
|---|---|
| **Asset afectado** | Audit logs / historial de acciones administrativas |
| **Descripción de la amenaza** | Un administrador realiza una acción crítica — elimina un usuario, modifica una política ABAC, revoca sesiones masivamente — y luego niega haberlo hecho. Sin logs inmutables el sistema no puede probarlo. |
| **Vector de ataque** | Ausencia de logs inmutables que registren quién ejecutó cada acción, desde dónde y cuándo |
| **Impacto si tiene éxito** | Imposibilidad de atribuir acciones críticas a usuarios específicos, comprometiendo investigaciones forenses y auditorías de cumplimiento |
| **Mitigación** | Audit logs inmutables con hash chaining que registran identidad, acción, timestamp e IP de cada operación — ya implementado como feature core del sistema |
| **Severidad** | Media |

---

### I — Information Disclosure

#### I-01: Exposición de datos sensibles en respuestas de error

| Campo | Detalle |
|---|---|
| **Asset afectado** | Información interna del sistema — estructura de base de datos, stack técnico, lógica de negocio |
| **Descripción de la amenaza**  | El sistema retorna mensajes de error demasiado detallados — stack traces, nombres de tablas, estructura interna — que le dan al atacante información valiosa sobre la arquitectura del sistema. |
| **Vector de ataque** | Manejo de errores mal configurado que expone excepciones internas en las respuestas de la API |
| **Impacto si tiene éxito** | El atacante obtiene información sobre la arquitectura interna que facilita ataques más sofisticados como SQL injection o JWT manipulation |
| **Mitigación** | Manejo centralizado de errores que retorna mensajes genéricos al cliente y registra el detalle completo solo en logs internos |
| **Severidad** | Media |

---

### D — Denial of Service

#### D-01: Authentication Endpoint Flooding

| Campo | Detalle |
|---|---|
| **Asset afectado** | Disponibilidad del servicio de autenticación |
| **Descripción de la amenaza** | El atacante bombardea el endpoint de autenticación con miles de requests por segundo, saturando el servidor y dejando a usuarios legítimos sin poder autenticarse. |
| **Vector de ataque** | Ausencia de rate limiting distribuido en el endpoint de login — un atacante puede generar requests ilimitados desde múltiples IPs |
| **Impacto si tiene éxito** | El servicio de autenticación queda inaccesible, bloqueando el acceso de todos los usuarios legítimos a cualquier aplicación que dependa de esta plataforma |
| **Mitigación** | Rate limiting distribuido con algoritmo Token Bucket — ya implementado como feature core del sistema. Bloqueo automático de IPs con comportamiento anómalo detectado por el motor de ML |
| **Severidad** | Alta |

---

## Resumen de severidad

| ID | Amenaza | Categoría STRIDE | Severidad |
|---|---|---|---|
| E-01 | JWT Manipulation | Elevation of Privilege | Crítica|
| E-02 | ABAC Policy Misconfiguration | Elevation of Privilege | Alta |
| E-03 | Token Reuse After Revocation | Elevation of Privilege | Crítica |
| S-01 | Credential Stuffing  | Spoofing | Alta |
| S-02 | Phishing de tokens | Spoofing | Alta |
| T-01 |Audit Log Tampering | Tampering | Alta |
| R-01 | Negación de acciones administrativas | Repudiation | Media |
| I-01 | Exposición de datos sensibles en respuestas de error | Information Disclosure | Media |
| D-01 | Authentication Endpoint Flooding | Denial of Service | Alta |

---

## Referencias

- [STRIDE Threat Model — Microsoft](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Security Best Practices — IETF RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725)
