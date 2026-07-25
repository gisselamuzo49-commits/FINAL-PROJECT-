# language: es
Característica: Gestión de Notificaciones y Seguridad de Acceso
  Como usuario del sistema
  Quiero ver y gestionar mis notificaciones
  Para mantenerme al día con mis trámites de pasantía y asegurar mis datos

  Escenario: Camino de éxito - Usuario marca notificación como leída
    Dado que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
    Y navega a la sección de "Notificaciones"
    Cuando selecciona una notificación pendiente
    Y hace clic en "Marcar como leída"
    Entonces la notificación debe cambiar su estado a leída y desaparecer del listado de pendientes

  Escenario: Camino alterno - Listado filtrado de notificaciones
    Dado que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
    Y navega a la sección de "Notificaciones"
    Cuando selecciona el filtro de notificaciones "Leídas"
    Entonces debe visualizar la lista que contiene solo las notificaciones leídas previamente

  Escenario: Excepción - Acceso directo sin token de autenticación
    Dado que el usuario no ha iniciado sesión en la plataforma
    Cuando intenta realizar una petición GET directa a "/api/notifications" en el gateway
    Entonces la petición debe ser rechazada con código "401" o "403" por el Gateway
