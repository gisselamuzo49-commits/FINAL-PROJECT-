import { useState } from 'react'

function App() {
  // --- ESTADOS PARA EL MICROSERVICIO DE USUARIOS (Puerto 8080) ---
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [mensajeAuth, setMensajeAuth] = useState("")

  // --- ESTADOS PARA EL MICROSERVICIO DE PASANTÍAS (Puerto 8081) ---
  const [pasantias, setPasantias] = useState([])
  const [mensajePasantias, setMensajePasantias] = useState("")

  // Función 1: Guardar Usuario
  const registrarUsuario = (e) => {
    e.preventDefault()
    fetch("http://54.81.204.136:8080/api/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, password: password })
    })
      .then(response => response.text())
      .then(data => setMensajeAuth(data))
      .catch(error => setMensajeAuth("Error: " + error.message))
  }

  // Función 2: Traer Pasantías (¡NUEVO!)
  const cargarPasantias = () => {
    fetch("http://54.81.204.136:8081/api/internships")
      .then(response => response.json()) // Ojo: Aquí recibimos JSON (una lista), no texto simple
      .then(data => {
        setPasantias(data)
        setMensajePasantias("¡Ofertas cargadas exitosamente!")
      })
      .catch(error => setMensajePasantias("Error al conectar: " + error.message))
  }

  return (
    <div style={{ textAlign: 'center', marginTop: '30px', fontFamily: 'sans-serif' }}>
      <h1>Sistema de Pasantías 🚀</h1>
      <p>Un Frontend conectado a DOS Microservicios simultáneamente</p>

      <div style={{ display: 'flex', justifyContent: 'center', gap: '20px', flexWrap: 'wrap', marginTop: '30px' }}>

        {/* --- COLUMNA 1: USUARIOS (8080) --- */}
        <div style={{ width: '350px', textAlign: 'left', padding: '20px', backgroundColor: '#e0f7fa', borderRadius: '8px', border: '2px solid #006064' }}>
          <h2 style={{ color: '#006064', marginTop: 0 }}>👤 1. Registro (Auth-Service)</h2>
          <form onSubmit={registrarUsuario}>
            <div style={{ marginBottom: '10px' }}>
              <label>Correo Electrónico:</label><br />
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required style={{ padding: '8px', width: '90%' }} />
            </div>
            <div style={{ marginBottom: '15px' }}>
              <label>Contraseña:</label><br />
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required style={{ padding: '8px', width: '90%' }} />
            </div>
            <button type="submit" style={{ padding: '10px', width: '95%', backgroundColor: '#006064', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
              Crear Cuenta
            </button>
          </form>
          {mensajeAuth && <p style={{ color: '#004d40', fontWeight: 'bold', marginTop: '15px' }}>{mensajeAuth}</p>}
        </div>

        {/* --- COLUMNA 2: PASANTÍAS (8081) --- */}
        <div style={{ width: '350px', textAlign: 'left', padding: '20px', backgroundColor: '#e8f5e9', borderRadius: '8px', border: '2px solid #2e7d32' }}>
          <h2 style={{ color: '#2e7d32', marginTop: 0 }}>💼 2. Tablero (Internship-Service)</h2>
          <button onClick={cargarPasantias} style={{ padding: '10px', width: '100%', backgroundColor: '#2e7d32', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', marginBottom: '15px' }}>
            Traer Ofertas de Trabajo
          </button>
          {mensajePasantias && <p style={{ color: '#1b5e20', fontWeight: 'bold' }}>{mensajePasantias}</p>}

          {/* Aquí se dibujan las tarjetas de cada pasantía que encuentre */}
          {pasantias.map((pasantia) => (
            <div key={pasantia.id} style={{ backgroundColor: 'white', padding: '15px', borderRadius: '5px', marginBottom: '10px', borderLeft: '5px solid #2e7d32', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
              <h3 style={{ margin: '0 0 5px 0', fontSize: '18px' }}>{pasantia.title}</h3>
              <p style={{ margin: '0 0 5px 0', fontSize: '14px' }}><strong>🏢 Empresa:</strong> {pasantia.company}</p>
              <p style={{ margin: '0 0 10px 0', fontSize: '14px', color: '#555' }}>{pasantia.description}</p>
              <span style={{ backgroundColor: '#c8e6c9', color: '#1b5e20', padding: '4px 8px', borderRadius: '12px', fontSize: '12px', fontWeight: 'bold' }}>
                {pasantia.status}
              </span>
            </div>
          ))}
        </div>

      </div>
    </div>
  )
}

export default App