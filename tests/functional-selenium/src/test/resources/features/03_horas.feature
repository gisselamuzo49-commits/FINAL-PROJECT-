# language: es
Característica: Registro y Validación de Horas de Vinculación
  Como estudiante y tutor del sistema
  Quiero registrar y validar las horas de prácticas preprofesionales
  Para llevar el control del cumplimiento de horas

  Escenario: Camino de éxito - Estudiante registra horas correctamente
    Dado que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
    Y navega a la sección de "Registro de Horas"
    Cuando registra un bloque de "10" horas de actividades con fecha de hoy
    Y hace clic en enviar
    Entonces el registro de horas es creado en estado "PENDIENTE"

  Escenario: Camino alterno - Tutor valida el registro de horas
    Dado que existe un registro de horas en estado "PENDIENTE"
    Cuando el tutor inicia sesión con "tutor@uce.edu.ec" y "password123"
    Y navega a la sección de validación de horas
    Y aprueba las horas registradas por el estudiante
    Entonces el estado de las horas cambia a "VALIDADO"

  Escenario: Excepción - Registro de horas negativas o con fecha futura (Defecto DEF-API-001)
    Dado que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
    Y navega a la sección de "Registro de Horas"
    Cuando intenta registrar "-5" horas o con fecha en el futuro
    Y hace clic en enviar
    Entonces el sistema no debería permitir el envío
    Y debería mostrar un error de validación en la interfaz
