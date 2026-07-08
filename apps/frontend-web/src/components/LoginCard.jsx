import { useState } from 'react';

function LoginCard({ setToken, setUserEmail }) {
  const [isRegisterMode, setIsRegisterMode] = useState(false);
  const [emailLogin, setEmailLogin] = useState("");
  const [passwordLogin, setPasswordLogin] = useState("");
  
  // Registration States
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [mensajeAuth, setMensajeAuth] = useState(null);

  const registrarUsuario = (e) => {
    e.preventDefault();
    setMensajeAuth(null);
    fetch(`/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email, password: password })
    })
      .then(response => {
        if (!response.ok) throw new Error("Error en el registro");
        return response.text();
      })
      .then(data => {
        setMensajeAuth({ text: data || "¡Usuario registrado con éxito!", type: "success" });
        setEmail("");
        setPassword("");
        setIsRegisterMode(false);
      })
      .catch(error => setMensajeAuth({ text: "Error: " + error.message, type: "error" }));
  };

  const iniciarSesion = (e) => {
    e.preventDefault();
    setMensajeAuth(null);
    fetch(`/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: emailLogin, password: passwordLogin })
    })
      .then(response => {
        if (!response.ok) throw new Error("Credenciales inválidas o error de conexión");
        return response.json();
      })
      .then(data => {
        localStorage.setItem("token", data.token);
        localStorage.setItem("userEmail", data.email);
        setToken(data.token);
        setUserEmail(data.email);
      })
      .catch(error => setMensajeAuth({ text: "Error: " + error.message, type: "error" }));
  };

  return (
    <div className="login-container">
      <div className="login-card glass-card">
        {isRegisterMode ? (
          <>
            <h2>Crear Cuenta de Acceso 👤</h2>
            {mensajeAuth && (
              <div className={`message-box ${mensajeAuth.type}`}>
                {mensajeAuth.text}
              </div>
            )}
            <form onSubmit={registrarUsuario}>
              <div className="form-group">
                <label className="form-label">Correo Electrónico:</label>
                <input
                  type="email"
                  className="form-input"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="estudiante@uce.edu.ec"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Contraseña:</label>
                <input
                  type="password"
                  className="form-input"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </div>
              <button type="submit" className="btn-primary">Registrarse</button>
            </form>
            <p>
              ¿Ya tienes una cuenta?{" "}
              <button className="btn-link" onClick={() => { setIsRegisterMode(false); setMensajeAuth(null); }}>
                Inicia Sesión
              </button>
            </p>
          </>
        ) : (
          <>
            <h2>Iniciar Sesión 🔐</h2>
            {mensajeAuth && (
              <div className={`message-box ${mensajeAuth.type}`}>
                {mensajeAuth.text}
              </div>
            )}
            <form onSubmit={iniciarSesion}>
              <div className="form-group">
                <label className="form-label">Correo Electrónico:</label>
                <input
                  type="email"
                  className="form-input"
                  value={emailLogin}
                  onChange={(e) => setEmailLogin(e.target.value)}
                  placeholder="estudiante@uce.edu.ec"
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Contraseña:</label>
                <input
                  type="password"
                  className="form-input"
                  value={passwordLogin}
                  onChange={(e) => setPasswordLogin(e.target.value)}
                  placeholder="••••••••"
                  required
                />
              </div>
              <button type="submit" className="btn-primary">Ingresar</button>
            </form>
            <p>
              ¿No tienes cuenta?{" "}
              <button className="btn-link" onClick={() => { setIsRegisterMode(true); setMensajeAuth(null); }}>
                Regístrate
              </button>
            </p>
          </>
        )}
      </div>
    </div>
  );
}

export default LoginCard;
