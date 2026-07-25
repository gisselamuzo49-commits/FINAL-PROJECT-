# language: es
Característica: Flujo de Postulaciones a Pasantías
  Como estudiante de la universidad
  Quiero postularme a las ofertas de pasantías disponibles
  Para poder realizar mis prácticas preprofesionales

  Antecedentes:
    Dado que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"

  Escenario: Camino de éxito - Estudiante realiza una postulación correctamente
    Dado que el estudiante navega a la sección de "Pasantías"
    Cuando selecciona una oferta de pasantía disponible
    Y hace clic en el botón "Postularse"
    Entonces la postulación debería ser creada exitosamente
    Y debería mostrarse en el historial de postulaciones en estado "PENDIENTE"

  Escenario: Camino alterno - Coordinador cambia el estado de la postulación
    Dado que existe una postulación en estado "PENDIENTE" para la oferta
    Cuando el coordinador inicia sesión con "coordinador@uce.edu.ec" y "password123"
    Y navega a la sección de postulaciones recibidas
    Y cambia el estado de la postulación del estudiante a "ACEPTADA"
    Entonces el estado de la postulación en la base de datos y la interfaz debe figurar como "ACEPTADA"

  Escenario: Excepción - Intento de postulación con campos vacíos
    Dado que el estudiante está en el formulario de postulación
    Cuando intenta enviar la postulación dejando los campos requeridos vacíos
    Entonces la interfaz debe bloquear el envío y mostrar un mensaje de error "Campos requeridos faltantes"
