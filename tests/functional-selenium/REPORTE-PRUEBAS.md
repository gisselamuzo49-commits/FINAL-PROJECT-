# REPORTE DE PRUEBAS FUNCIONALES — Sistema de Pasantías y Vinculación UCE

**Generado automáticamente el:** 2026-07-24 23:37:47
**Entorno:** QA — `http://54.227.79.26`
**Framework:** Cucumber 7 + Selenium 4 + JUnit 5

---

## Índice de Casos de Prueba

1. [Login y Navegación de Usuarios](#caso-1)
2. [Flujo de Postulaciones a Pasantías](#caso-2)
3. [Registro y Validación de Horas de Vinculación](#caso-3)
4. [Calificación y Evaluaciones de Pasantías](#caso-4)
5. [Gestión de Notificaciones y Seguridad de Acceso](#caso-5)

---

## Caso de prueba 1: Login y Navegación de Usuarios

**Objetivo:** Verificar que los usuarios (Estudiante y Tutor) pueden autenticarse correctamente y son redirigidos al panel según su rol, y que credenciales incorrectas son rechazadas.

**Resultado general:** ❌ FAIL

### Camino de éxito

**Escenario:** Camino de éxito - Estudiante puede hacer login correctamente

**Pasos ejecutados:**
- que el usuario navega a la página de login
- ingresa el correo "estudiante@uce.edu.ec" y la contraseña "password123"
- hace clic en el botón de iniciar sesión
- el usuario debería ser redirigido a la página de inicio "/home"
- debería ver el menú de navegación con "Mis Postulaciones"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Camino alterno

- ⚠️ Escenario no ejecutado o no encontrado en cucumber.json.

### Excepción

**Escenario:** Excepción - Intento de login con credenciales incorrectas

**Pasos ejecutados:**
- que el usuario navega a la página de login
- ingresa el correo "invalido@uce.edu.ec" y la contraseña "error123"
- hace clic en el botón de iniciar sesión
- debería ver un mensaje de alerta que dice "Credenciales incorrectas"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

---

## Caso de prueba 2: Flujo de Postulaciones a Pasantías

**Objetivo:** Verificar que un estudiante puede postularse a una oferta (quedando en PENDIENTE), que el coordinador puede cambiar el estado (ACEPTADA/RECHAZADA) y que campos vacíos impiden el envío.

**Resultado general:** ❌ FAIL

### Camino de éxito

**Escenario:** Camino de éxito - Estudiante realiza una postulación correctamente

**Pasos ejecutados:**
- que el estudiante navega a la sección de "Pasantías"
- selecciona una oferta de pasantía disponible
- hace clic en el botón "Postularse"
- la postulación debería ser creada exitosamente
- debería mostrarse en el historial de postulaciones en estado "PENDIENTE"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Camino alterno

**Escenario:** Camino alterno - Coordinador cambia el estado de la postulación

**Pasos ejecutados:**
- que existe una postulación en estado "PENDIENTE" para la oferta
- el coordinador inicia sesión con "coordinador@uce.edu.ec" y "password123"
- navega a la sección de postulaciones recibidas
- cambia el estado de la postulación del estudiante a "ACEPTADA"
- el estado de la postulación en la base de datos y la interfaz debe figurar como "ACEPTADA"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Excepción

**Escenario:** Excepción - Intento de postulación con campos vacíos

**Pasos ejecutados:**
- que el estudiante está en el formulario de postulación
- intenta enviar la postulación dejando los campos requeridos vacíos
- la interfaz debe bloquear el envío y mostrar un mensaje de error "Campos requeridos faltantes"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

---

## Caso de prueba 3: Registro y Validación de Horas de Vinculación

**Objetivo:** Verificar que un estudiante puede registrar horas válidas (en PENDIENTE), que el tutor puede validarlas (a VALIDADO/RECHAZADO), y que valores inválidos (negativos o fecha futura) son rechazados.

**Resultado general:** ❌ FAIL

### Camino de éxito

**Escenario:** Camino de éxito - Estudiante registra horas correctamente

**Pasos ejecutados:**
- que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
- navega a la sección de "Registro de Horas"
- registra un bloque de "10" horas de actividades con fecha de hoy
- hace clic en enviar
- el registro de horas es creado en estado "PENDIENTE"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Camino alterno

**Escenario:** Camino alterno - Tutor valida el registro de horas

**Pasos ejecutados:**
- que existe un registro de horas en estado "PENDIENTE"
- el tutor inicia sesión con "tutor@uce.edu.ec" y "password123"
- navega a la sección de validación de horas
- aprueba las horas registradas por el estudiante
- el estado de las horas cambia a "VALIDADO"

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Excepción

**Escenario:** Excepción - Registro de horas negativas o con fecha futura (Defecto DEF-API-001)

**Pasos ejecutados:**
- que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
- navega a la sección de "Registro de Horas"
- intenta registrar "-5" horas o con fecha en el futuro
- hace clic en enviar
- el sistema no debería permitir el envío
- debería mostrar un error de validación en la interfaz

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

> ⚠️ **FALLA ESPERADA** — relacionada a DEF-API-001 (ausencia de Bean Validation en hours-service)

---

## Caso de prueba 4: Calificación y Evaluaciones de Pasantías

**Objetivo:** Verificar que el tutor puede registrar evaluaciones con calificación 0-10, que valores fuera de rango son bloqueados, y que el acceso de estudiantes a las evaluaciones de tutores es denegado.

**Resultado general:** ❌ FAIL

### Camino de éxito

**Escenario:** Camino de éxito - Tutor califica al estudiante con nota válida

**Pasos ejecutados:**
- que el tutor ha iniciado sesión con "tutor@uce.edu.ec" y "password123"
- navega a la sección de "Evaluaciones"
- registra una evaluación con calificación "9" y comentario "Excelente desempeño en sus tareas"
- envía el formulario
- la evaluación debe registrarse correctamente

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Camino alterno

**Escenario:** Camino alterno - Tutor intenta ingresar nota fuera de rango

**Pasos ejecutados:**
- que el tutor ha iniciado sesión con "tutor@uce.edu.ec" y "password123"
- navega a la sección de "Evaluaciones"
- ingresa una calificación de "15"
- el formulario de evaluación debe impedir el envío y mostrar error de rango

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Excepción

**Escenario:** Excepción - Estudiante intenta ingresar a evaluaciones de tutor

**Pasos ejecutados:**
- que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
- intenta navegar directamente a la URL de evaluaciones de tutor "/evaluations"
- el sistema debe denegar el acceso o redirigir al home por falta de permisos

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

---

## Caso de prueba 5: Gestión de Notificaciones y Seguridad de Acceso

**Objetivo:** Verificar que un usuario puede marcar notificaciones como leídas, filtrar el listado y que el acceso al API de notificaciones sin token JWT es rechazado con 401/403 por el Gateway.

**Resultado general:** ❌ FAIL

### Camino de éxito

**Escenario:** Camino de éxito - Usuario marca notificación como leída

**Pasos ejecutados:**
- que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
- navega a la sección de "Notificaciones"
- selecciona una notificación pendiente
- hace clic en "Marcar como leída"
- la notificación debe cambiar su estado a leída y desaparecer del listado de pendientes

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Camino alterno

**Escenario:** Camino alterno - Listado filtrado de notificaciones

**Pasos ejecutados:**
- que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
- navega a la sección de "Notificaciones"
- selecciona el filtro de notificaciones "Leídas"
- debe visualizar la lista que contiene solo las notificaciones leídas previamente

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

### Excepción

**Escenario:** Excepción - Acceso directo sin token de autenticación

**Pasos ejecutados:**
- que el usuario no ha iniciado sesión en la plataforma
- intenta realizar una petición GET directa a "/api/notifications" en el gateway
- la petición debe ser rechazada con código "401" o "403" por el Gateway

**Resultado:** ❌ FAIL

**Captura:** _No disponible (ejecutar tests para generar capturas)_

---

## Tabla Resumen de Resultados

| # | Caso de Prueba | Camino éxito | Camino alterno | Excepción | Defecto relacionado |
|---|----------------|:------------:|:--------------:|:---------:|---------------------|
| 1 | Login y Navegación de Usuarios | ❌ FAIL | — | ❌ FAIL | — |
| 2 | Postulaciones a Pasantías | ❌ FAIL | ❌ FAIL | ❌ FAIL | — |
| 3 | Registro y Validación de Horas de Vinculación | ❌ FAIL | ❌ FAIL | ❌ FAIL | DEF-API-001 (ausencia de Bean Validation en hours-service) |
| 4 | Calificación y Evaluaciones de Pasantías | ❌ FAIL | ❌ FAIL | ❌ FAIL | — |
| 5 | Notificaciones y Seguridad de Acceso | ❌ FAIL | ❌ FAIL | ❌ FAIL | — |

---

*Reporte generado por `generate_report.py` — Sistema de Pasantías UCE*
