import { useState, useEffect } from 'react'

function App() {
  const [mensaje, setMensaje] = useState("Esperando al backend...")

  useEffect(() => {
    // ¡Aquí es donde ocurre la magia de la conexión!
    fetch("http://localhost:8080/api/auth/hello")
      .then(response => response.text())
      .then(data => setMensaje(data))
      .catch(error => setMensaje("Error al conectar: " + error.message))
  }, [])

  return (
    <div style={{ textAlign: 'center', marginTop: '50px', fontFamily: 'sans-serif' }}>
      <h1>Sistema de Pasantías 🚀</h1>
      <div style={{ padding: '20px', backgroundColor: '#e0f7fa', borderRadius: '10px', display: 'inline-block' }}>
        <h2>Mensaje recibido del Servidor Java:</h2>
        <p style={{ fontSize: '20px', color: '#006064', fontWeight: 'bold' }}>{mensaje}</p>
      </div>
    </div>
  )
}

export default App