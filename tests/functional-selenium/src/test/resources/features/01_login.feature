# language: es
Característica: Login y Navegación de Usuarios
  Como usuario del sistema (Estudiante o Tutor)
  Quiero autenticarme en la plataforma
  Para poder acceder a mis funcionalidades correspondientes

  Escenario: Camino de éxito - Estudiante puede hacer login correctamente
    Dado que el usuario navega a la página de login
    Cuando ingresa el correo "estudiante@uce.edu.ec" y la contraseña "password123"
    Y hace clic en el botón de iniciar sesión
    Entonces el usuario debería ser redirigido a la página de inicio "/home"
    Y debería ver el menú de navegación con "Mis Postulaciones"

  Escenario: Camino alterno - Tutor puede hacer login correctamente
    Dado que el usuario navega a la página de login
    Cuando ingresa el correo "tutor@uce.edu.ec" y la contraseña "password123"
    Y hace clic en el botón de iniciar sesión
    Entonces el usuario debería ser redirigido a la página de inicio "/home"
    Y no debería ver la opción de "Mis Postulaciones" en el panel de estudiante

  Escenario: Excepción - Intento de login con credenciales incorrectas
    Dado que el usuario navega a la página de login
    Cuando ingresa el correo "invalido@uce.edu.ec" y la contraseña "error123"
    Y hace clic en el botón de iniciar sesión
    Entonces debería ver un mensaje de alerta que dice "Credenciales incorrectas"
