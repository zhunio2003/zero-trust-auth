# PRODUCT VISION
**Proyecto:** Zero Trust AUTH  
**Metodología:** Scrum  
**Versión del documento:** 1.0  
**Fecha:** 24 de Marzo del 2026  
**Autor:** Miguel Angel Zhunio

---

## 1. Vision
Para Tech Leads en empresas medianas que necesitan proteger datos sensibles más allá de un login básico, ZeroTrust Auth Platform es una plataforma de autenticacion y autorizacion empresarial que verifica continuamente cada acceso en funcion de identidad, contexto y comportamiento — no solo en el momento del login.
A diferencia de Auth0, que delega toda la seguridad al perímetro de autenticacion inicial, ZeroTrust implementa un modelo de confianza cero real: cada request es evaluado con controles como autorizacion basada en atributos (ABAC), deteccion de anomalias con machine learning y audit logs inmutables con hash chaining — haciendo imposible el acceso no autorizado incluso con credenciales comprometidas.

---

## 2. Grupo Objectivo

### 2.1 Tech lead
Es el lider tecnico del equipo de desarrollo que necesita proteger los datos sensibles de la empresa. Este usuario tendra un conocimiento medio alto. Ademas de estar encargado de monitorear constatemente la seguridad de dichos datos. Este usuario espera que este sistema sea el encargado de implementar constantemente todas las medidad modernas de seguridad  por ejemplo ABAC, deteccion de anomalias, audit logs inmutables y ofrecer un dashboard interactivo interactivo que muestre el flujo de seguridad el sistema y notifique al usuario sobe anomalias. Este usuario accedera al sistema de una manera recurrente y cuando sea notificado.

---

### 2.2 Backend developer
Es un desarrollador de software que puede tener conocimientos en seguridad como no puede tenerlos. En los dos escenarios compartiran la misma necesidad la cual es implementar un sistema que les ahorre/facilite la implementacion de las medidas de seguridad (ABAC, deteccion de anomalias, audid,l log inmutables, etc). Este usuario esta encargado de estar en constante monitoreo de la seguiridad del usuario. Ademas este usuario espera que el sistema le exponga endpoints de health, metricas y notificaciones entre otras, lo cual le posibilite crear su propio seccion del sistema y no limitarse solo del dashboard del ZeroTrust auth.

--- 

### 2.3 Administrador del sistema
Es el encargado de la gestion del usuarios y el monitoreo de sus actividades dentro sistema.Para ello necesitara un dashboard que le permita gestionar y monitorear a estos usuarios. Su frecuencia del uso del sistema sera en todo momento ya que sera su herramienta para realizar su trabajo.

---

## 3. Necesidades

### 3.1 Tech Lead 
Las medidas básicas de seguiridad  no persisten después del login, dejando una brecha de seguridad con usuarios ya autenticados permitiendo un posible explotacion de este acceso.

### 3.2 Backend Developer 
Implementar cada medida de seguridad por separado requiere conocimiento profundo de cada una, lo cual consume tiempo y expertise que no siempre se tiene. Y que conlleva mas tiempo en el desarrollo de los sistemas.

### 3.3 Administrador 
Gestionar usuarios, sesiones y permisos sin una herramienta centralizada requiere conocimientos técnicos de backend que no debería necesitar para hacer su trabajo operativo. 

--- 

## 4. Propuesta de valor

### 4.1 Tech lead
Tech load tiene la posibilidad de monitorear el flujo de seguridad en tiempo real y de notificacion, lo que le permite estar al pendiente de cualquier situacion que exponga a riesgos a nuestros usuarios y sistema y contrestrar a tiempo. Todo esto sin la necesidad de planificar, diseñar y programar dichas medidas de seguridad, solo consumir nuestro servicio

### 4.2 Backend Developer
Backend developer le permite implementar la capa de seguridad mediante el consumo de la API de zero trust la cual le ofrecera todas medidas de seguiridad presentadas en el mercado y su monitoreo en tiempo real, ademas de ofrecerlos dos caminos para poder visualizar los resultados de los mismos los cuales seran mediante un dashboard del mismo sistema de zerotrust o mediante un conjunto de endpoints que proporcioara health, metricas y estadisticas en tiempo real y la notificacion de anomalias entre otras.

### 4.3 Administrador
Admistrador Le proporciona un dashboard de gestion de usuarios lo que facilita la gestion de los usuarios,roles, permisos y demas. Todo eto sin necesidad de acceder a la base de datos o al backend del sistema.

---

## 5. Objetivos de Negocio

### 5.1 Adopcion 
Alcanzar entre 500 y 1,000 usuarios activos y 100 empresas durante el primer año de operaciones.

### 5.2 Retención

- **API calls mensuales**: El cliente aumenta o mantiene el número de llamadas a la API, o si el cliente sigue activo y usando el sistema.
- **Tiempo de integración activa**: El tiempo que lleva un cliente usando la plataforma sin abandonarla.
- **Expansión**: Los desarrolladores que han integrado ZeroTrust en un proyecto y luego lo integran en un mas de sus proyectos.
- Metricas **NPS** en todos los dashboards.

### 5.3 Calidad

- **MTTD — Mean Time to Detect**: El sistema debe detectar una anomalia lo mas pronto posible. Entre más bajo mejor.
- **MTTA — Mean Time to Acknowledge**: El sistema debe notificar lo antes posible mediantes los medios de comunicacion las anomalias detectadas para su rapida solucion.
- **False Positive Rate**: El sistema debe disminir consoderable la alerta de falsos positivos. Si el sistema alerta demasiado con cosas normales, el administrador deja de hacerle caso.
- **Uptime**: El sistema debe estar disponible 99.9% del tiempo. Si el auth se cae, nadie entra a la aplicación (Tanto el sistema zeroTrust y el sistema que lo implemento).


