import { useState } from 'react'

function App() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [mensaje, setMensaje] = useState("")

  const registrarUsuario = (e) => {
    e.preventDefault() // Evita que la página se recargue al dar clic

    // Le enviamos los datos de los inputs a tu Spring Boot
    fetch("http://localhost:8080/api/auth/register", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ email: email, password: password })
    })
      .then(response => response.text())
      .then(data => setMensaje(data))
      .catch(error => setMensaje("Error al conectar: " + error.message))
  }

  return (
    <div style={{ textAlign: 'center', marginTop: '50px', fontFamily: 'sans-serif' }}>
      <h1>Sistema de Pasantías 🚀</h1>
      <h2>Registro de Nuevos Estudiantes</h2>

      {/* Formulario básico sin diseño complejo */}
      <form onSubmit={registrarUsuario} style={{ display: 'inline-block', textAlign: 'left', padding: '20px', backgroundColor: '#f5f5f5', borderRadius: '8px' }}>

        <div style={{ marginBottom: '10px' }}>
          <label>Correo Electrónico: </label> <br />
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ padding: '5px', width: '200px' }}
          />
        </div>

        <div style={{ marginBottom: '15px' }}>
          <label>Contraseña: </label> <br />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ padding: '5px', width: '200px' }}
          />
        </div>

        <button type="submit" style={{ padding: '10px 20px', cursor: 'pointer', backgroundColor: '#006064', color: 'white', border: 'none', borderRadius: '5px', width: '100%' }}>
          Crear Cuenta
        </button>
      </form>

      {/* Aquí mostramos la respuesta que nos dé tu base de datos */}
      {mensaje && (
        <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#e0f7fa', color: '#006064', borderRadius: '5px', display: 'inline-block' }}>
          <strong>Respuesta del servidor: </strong> {mensaje}
        </div>
      )}
    </div>
  )
}

export default App