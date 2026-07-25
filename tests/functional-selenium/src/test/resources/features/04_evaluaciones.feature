# language: es
Característica: Calificación y Evaluaciones de Pasantías
  Como tutor académico
  Quiero calificar el desempeño del estudiante en sus pasantías
  Para registrar su evaluación final

  Escenario: Camino de éxito - Tutor califica al estudiante con nota válida
    Dado que el tutor ha iniciado sesión con "tutor@uce.edu.ec" y "password123"
    Y navega a la sección de "Evaluaciones"
    Cuando registra una evaluación con calificación "9" y comentario "Excelente desempeño en sus tareas"
    Y envía el formulario
    Entonces la evaluación debe registrarse correctamente

  Escenario: Camino alterno - Tutor intenta ingresar nota fuera de rango
    Dado que el tutor ha iniciado sesión con "tutor@uce.edu.ec" y "password123"
    Y navega a la sección de "Evaluaciones"
    Cuando ingresa una calificación de "15"
    Entonces el formulario de evaluación debe impedir el envío y mostrar error de rango

  Escenario: Excepción - Estudiante intenta ingresar a evaluaciones de tutor
    Dado que el estudiante ha iniciado sesión con "estudiante@uce.edu.ec" y "password123"
    Cuando intenta navegar directamente a la URL de evaluaciones de tutor "/evaluations"
    Entonces el sistema debe denegar el acceso o redirigir al home por falta de permisos
