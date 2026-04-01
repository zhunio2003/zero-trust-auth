# Definition of Done — ZeroTrust Auth Platform

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Versión:** 1.0  
**Fecha:** 2026-03-31  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. Introducción

Este documento define el **Definition of Done (DoD)** del proyecto ZeroTrust Auth Platform: el acuerdo formal que establece cuándo un incremento de trabajo se considera terminado. El DoD es transversal a todo el proyecto y se aplica de forma consistente en cada nivel de entrega.

A diferencia de proyectos de propósito general, ZeroTrust Auth Platform tiene **seguridad como core del sistema**, no como afterthought. Esto se refleja estructuralmente en el DoD: la seguridad se verifica en los 3 niveles de entrega, comenzando desde cada historia individual.

El DoD se estructura en **3 niveles**, cada uno construido sobre el anterior:

| Nivel | Alcance | Pregunta que responde |
|-------|---------|----------------------|
| Nivel 1 | Historia de Usuario / Technical Story | ¿Esta historia está terminada? |
| Nivel 2 | Sprint (Incremento) | ¿Este sprint entregó un incremento funcional y seguro? |
| Nivel 3 | Release (Lanzamiento a Producción) | ¿Esto está listo para usuarios reales? |

**Documentos de referencia:**

- `docs/THREAT-MODEL_STRIDE.md` — Modelo de amenazas STRIDE con 9 amenazas identificadas y sus mitigaciones.
- `docs/DETAILED_ARCHITECTURE.md` — Arquitectura detallada con 4 microservicios y principios arquitectónicos.
- `docs/COMPONENT_DIAGRAM.md` — Diagrama de componentes por servicio.
- `docs/DEPLOYMENT_DIAGRAM.md` — Diagrama de despliegue con redes Docker segmentadas.
- `docs/TECHNOLOGY_STACK_ZEROTRUST.md` — Decisiones tecnológicas del proyecto.

---

## 2. Nivel 1 — DoD por Historia de Usuario / Technical Story

Cada Historia de Usuario o Technical Story debe cumplir **todos** los puntos siguientes para considerarse terminada. No se acepta una historia como completada si algún punto queda pendiente.

---

### 2.1 Código

1. El código está subido al repositorio (GitHub) en la rama correspondiente con Conventional Commits.
2. El código cumple los estándares de codificación verificados por el linter del lenguaje correspondiente:
   - Java: Checkstyle (Authentication Service, Authorization Service, Audit Log Service — Spring Boot).
   - Python: Flake8 (ML / Anomaly Detection + Policy Engine — FastAPI).
3. No contiene credenciales, claves privadas, URLs ni configuraciones hardcodeadas en el código fuente. Las configuraciones sensibles se gestionan mediante variables de entorno.
4. Se realizó auto-revisión estructurada del código antes de marcar la historia como terminada.

---

### 2.2 Testing

5. La historia tiene pruebas unitarias que cubren la lógica de negocio implementada.
6. La historia tiene pruebas de integración cuando el servicio interactúa con otros componentes (base de datos, Kafka, otros microservicios).
7. Toda historia que implemente autenticación o autorización debe incluir **negative security tests** que cubran los siguientes escenarios, con rechazo HTTP 401 en cada caso:
   - JWT con algoritmo de firma distinto a RS256 o con algoritmo `none`.
   - JWT con firma manipulada o inválida.
   - JWT con claims faltantes o incorrectos (`sub`, `role`, `department`, `exp`, `iat`).
8. Toda historia que implemente políticas ABAC debe incluir **policy unit tests** que cubran edge cases y combinaciones límite de condiciones, verificando denegación por defecto ante casos no cubiertos.
9. Todas las pruebas pasan exitosamente antes de considerar la historia como terminada.

**Frameworks de testing por lenguaje:**

| Lenguaje | Framework de pruebas | Componentes |
|----------|---------------------|-------------|
| Java | JUnit + Mockito | Authentication Service, Authorization Service, Audit Log Service |
| Python | Pytest | ML / Anomaly Detection + Policy Engine |

---

### 2.3 Despliegue

10. El servicio modificado se construye exitosamente como imagen Docker.
11. El pipeline de CI/CD (GitHub Actions) ejecuta las pruebas automáticamente y todas pasan.
12. El servicio está desplegado y funcionando en el ambiente de staging dentro de la red Docker correspondiente (`services`, `data` o `monitoring` según el contenedor).

---

### 2.4 Documentación

13. Los endpoints del servicio están documentados y accesibles a través de Swagger/OpenAPI (generados automáticamente por Spring Boot y FastAPI).
14. Si la historia introduce una decisión técnica relevante, se documenta como un ADR (Architecture Decision Record) en `docs/adr/`.

---

### 2.5 Criterios de Aceptación

15. Todos los criterios de aceptación definidos en la historia están cumplidos y verificados.

---

### 2.6 Integración con el Sistema

16. El servicio modificado no rompe funcionalidades existentes de otros servicios (verificado mediante pruebas de integración y validación en staging).
17. Si la historia afecta la comunicación entre servicios (nuevos eventos en Kafka, nuevos endpoints, cambios en contratos de API), los servicios consumidores están actualizados y funcionan correctamente.

---

### 2.7 Seguridad

18. El JWT emitido utiliza algoritmo RS256. Tokens con algoritmo `none` o firmas inválidas son rechazados con HTTP 401 sin excepciones ni fallbacks.
19. El JWT emitido contiene los claims mínimos requeridos: `sub`, `role`, `department`, `exp` e `iat`. Tokens con claims faltantes o incorrectos son rechazados con HTTP 401.
20. Los logs del servicio no contienen credenciales del usuario — email, contraseña ni tokens en texto plano — en ningún nivel de logging (debug, info, error).
21. Los mensajes de error retornados al cliente son genéricos y no especifican qué campo falló, qué tabla fue consultada ni detalles internos del sistema.

---

## 3. Nivel 2 — DoD de Sprint (Incremento)

Al finalizar cada sprint, el incremento entregado debe cumplir **todos** los puntos siguientes:

1. Todas las historias incluidas en el sprint cumplen el DoD de Historia (Nivel 1, los 21 puntos).
2. El incremento es funcional de extremo a extremo en el ambiente de staging. Los flujos completos que involucran las historias del sprint se verifican como operativos.
3. No se introducen regresiones: las funcionalidades terminadas en sprints anteriores siguen funcionando correctamente después de los cambios del sprint actual.
4. El Product Backlog está actualizado: las historias completadas están marcadas como terminadas, y las nuevas historias o cambios que surgieron durante el sprint están reflejados.
5. Si el sprint incluye historias de seguridad, el incremento debe pasar un **security integration test** que valide que los mecanismos de seguridad funcionan correctamente a través de todos los servicios involucrados en los flujos afectados.

---

## 4. Nivel 3 — DoD de Release (Lanzamiento a Producción)

Cuando se decide lanzar un conjunto de incrementos a producción, se deben cumplir **todos** los puntos siguientes:

1. Todos los incrementos incluidos en el release cumplen el DoD de Sprint (Nivel 2).
2. Los endpoints de autenticación y autorización responden en menos de 100ms en el percentil 95 (p95) bajo 1000 requests concurrentes. Si este criterio no se cumple, el release no se aprueba hasta que se resuelva.
3. La documentación de usuario está actualizada: las funcionalidades nuevas que el usuario final o el administrador van a utilizar están documentadas.
4. Se ejecutó la verificación de integridad del Audit Log Service: el componente **Log Query & Integrity** recalcula la cadena de hashes de todos los eventos almacenados y confirma que ningún evento fue alterado (hash chaining íntegro). Si la cadena está rota, el release no se aprueba.
5. Existe un plan de rollback documentado: si algo falla en producción, se puede revertir a la versión anterior de forma rápida y controlada.

---

## 5. Notas sobre la Evolución del DoD

El Definition of Done es un **documento vivo** que evoluciona con la madurez del proyecto. En la práctica de Scrum, es válido y recomendado revisar y fortalecer el DoD al finalizar cada Sprint Review o Retrospectiva.

Aspectos que se incorporarán progresivamente conforme el proyecto avance:

- **Cobertura mínima de pruebas:** Definición de un porcentaje mínimo de cobertura de código por pruebas unitarias (80% como objetivo).
- **Pruebas de contrato de API:** Verificación de que cada microservicio responde correctamente según su contrato definido (formato de entrada, formato de salida, códigos de error).
- **Pruebas de estrés:** Verificación del comportamiento del sistema bajo condiciones de alta demanda sostenida.

---

## 6. Resumen

| Nivel | Puntos | Se aplica |
|-------|--------|-----------|
| Nivel 1 — Historia | 21 puntos (7 dimensiones) | A cada historia individual |
| Nivel 2 — Sprint | 5 puntos | Al finalizar cada sprint |
| Nivel 3 — Release | 5 puntos | Al lanzar a producción |
| **Total** | **31 puntos de verificación** | |

---

## Referencias

- [Scrum Guide — Definition of Done](https://scrumguides.org/scrum-guide.html#increment)
- [STRIDE Threat Model — Microsoft](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats)
- [JWT Security Best Practices — IETF RFC 8725](https://datatracker.ietf.org/doc/html/rfc8725)
- [OWASP Testing Guide](https://owasp.org/www-project-web-security-testing-guide/)