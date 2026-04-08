# Sprint Retrospective — Sprint 1

**Proyecto:** ZeroTrust Auth Platform  
**Metodología:** Scrum — Security-first Agile  
**Sprint:** Sprint 1  
**Fecha:** 08 de abril de 2026  
**Autor:** Miguel Angel Zhunio Remache

---

## 1. ¿Qué salió bien?

**El orden de ejecución de las tareas.**

Cada historia estaba correctamente encadenada — no se podía avanzar a la siguiente sin completar la anterior. La estructura del Sprint Planning reflejó las dependencias reales del sistema: sin repositorio no hay pipeline, sin pipeline no hay Docker Compose, sin Docker Compose no hay Gateway.

Esta disciplina fue una lección aprendida a las malas en el proyecto MultIAZ, donde el orden de ejecución no fue respetado y generó retrabajo. En este sprint se aplicó desde el inicio y el resultado fue un sprint limpio, sin bloqueos por dependencias mal gestionadas.

---

## 2. ¿Qué salió mal o fue difícil?

**La curva de aprendizaje de las herramientas de infraestructura.**

El Docker Compose superó las 350 líneas y el pipeline CI/CD las 150 — archivos extensos que al principio generaron la percepción de que algo estaba mal. No era un problema de diseño sino de escala necesaria para un sistema con 14 contenedores y 5 jobs de CI.

Las tecnologías de infraestructura — Kafka, Prometheus y Grafana — representaron el mayor desafío del sprint. No por su complejidad conceptual sino por desconocimiento previo: no se sabía qué hacía cada herramienta antes de configurarla. Esto generó errores que consumieron tiempo innecesario:

- Kafka crasheaba repetidamente por configuración incorrecta de listeners — sin entender qué era un listener, la solución tomó múltiples iteraciones
- Prometheus fallaba en el healthcheck porque la imagen no tiene `curl` — sin saber cómo funciona el scraping, el diagnóstico fue lento
- Grafana y MongoDB tuvieron problemas similares de configuración que se habrían evitado con conocimiento previo básico

---

## 3. ¿Qué mejorar para el Sprint 2?

**Estudiar las bases de cada tecnología antes de implementarla.**

La lección concreta: dedicar 20-30 minutos a entender qué hace una herramienta, cuál es su modelo mental y qué configuración mínima necesita — antes de escribir una sola línea de configuración. No es necesario leer la documentación completa, pero sí tener claro el concepto.

Para Sprint 2 esto aplica directamente a las tecnologías que se van a usar por primera vez: JWT con RS256, TOTP/WebAuthn, Redis como blacklist de tokens y el algoritmo Token Bucket. Estudiar el concepto primero, implementar después.

---

## 4. Action Items

| # | Acción | Aplica en |
|---|--------|-----------|
| 1 | Antes de implementar una tecnología nueva, estudiar su concepto y modelo mental básico (20-30 min mínimo) | Sprint 2 en adelante |
| 2 | Documentar decisiones de configuración no obvias con comentarios en el archivo — especialmente en Docker Compose y CI/CD | Sprint 2 en adelante |

---

## Referencias

- [Sprint Review Sprint 1](SPRINT_REVIEW_SPRINT1.md)
- [Sprint Planning Sprint 1](../product/SPRINT_PLANNING_SPRINT1.md)
