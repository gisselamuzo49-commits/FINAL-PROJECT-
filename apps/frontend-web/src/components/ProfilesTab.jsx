import { useState, useEffect } from 'react';

function ProfilesTab({ API_URL, GATEWAY_PORT, getHeaders, logout }) {
  const [profiles, setProfiles] = useState([]);
  const [mensajeProfiles, setMensajeProfiles] = useState(null);

  // Form States
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [profileEmail, setProfileEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState("STUDENT");

  const cargarPerfiles = () => {
    fetch(`${API_URL}:${GATEWAY_PORT}/api/users`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al conectar con User-Service");
        return response.json();
      })
      .then(data => setProfiles(data))
      .catch(error => setMensajeProfiles({ text: "Error al cargar: " + error.message, type: "error" }));
  };

  const registrarPerfil = (e) => {
    e.preventDefault();
    setMensajeProfiles(null);
    fetch(`${API_URL}:${GATEWAY_PORT}/api/users`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ firstName, lastName, email: profileEmail, phone, role })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al crear perfil de usuario");
        return response.json();
      })
      .then(() => {
        setMensajeProfiles({ text: "¡Perfil de estudiante/tutor creado exitosamente!", type: "success" });
        setFirstName("");
        setLastName("");
        setProfileEmail("");
        setPhone("");
        setRole("STUDENT");
        cargarPerfiles();
      })
      .catch(error => setMensajeProfiles({ text: "Error: " + error.message, type: "error" }));
  };

  useEffect(() => {
    cargarPerfiles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="grid-layout">
      <div className="glass-card">
        <h2 className="card-title">👤 Crear Perfil Académico</h2>
        {mensajeProfiles && mensajeProfiles.type === "success" && (
          <div className="message-box success">{mensajeProfiles.text}</div>
        )}
        <form onSubmit={registrarPerfil}>
          <div className="grid-layout" style={{ gap: '16px', gridTemplateColumns: '1fr 1fr', marginBottom: '16px' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Nombre:</label>
              <input
                type="text"
                className="form-input"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                placeholder="Juan"
                required
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label className="form-label">Apellido:</label>
              <input
                type="text"
                className="form-input"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                placeholder="Pérez"
                required
              />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Correo Institucional:</label>
            <input
              type="email"
              className="form-input"
              value={profileEmail}
              onChange={(e) => setProfileEmail(e.target.value)}
              placeholder="juan.perez@uce.edu.ec"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Teléfono de Contacto:</label>
            <input
              type="tel"
              className="form-input"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="0998765432"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Rol en el Sistema:</label>
            <select
              className="form-select"
              value={role}
              onChange={(e) => setRole(e.target.value)}
            >
              <option value="STUDENT">Estudiante</option>
              <option value="TUTOR">Tutor Académico</option>
              <option value="COORDINATOR">Coordinador de Carrera</option>
            </select>
          </div>
          <button type="submit" className="btn-primary">Guardar Perfil</button>
        </form>
      </div>

      <div className="glass-card">
        <div className="refresh-section">
          <h2 className="card-title" style={{ margin: 0, border: 'none' }}>👤 Perfiles Registrados</h2>
          <button className="btn-secondary" onClick={cargarPerfiles}>🔄 Actualizar</button>
        </div>
        {mensajeProfiles && mensajeProfiles.type === "error" && (
          <div className="message-box error">{mensajeProfiles.text}</div>
        )}

        <div className="list-container">
          {profiles.length === 0 ? (
            <div className="empty-state">No hay perfiles de usuarios registrados.</div>
          ) : (
            profiles.map((prof) => (
              <div key={prof.id} className="list-item">
                <div className="item-header">
                  <h3 className="item-title">{prof.firstName} {prof.lastName}</h3>
                  <span className={`badge ${prof.role ? prof.role.toLowerCase() : 'student'}`}>
                    {prof.role}
                  </span>
                </div>
                <div className="item-meta">📧 <strong>Email:</strong> {prof.email}</div>
                <div className="item-meta">📞 <strong>Teléfono:</strong> {prof.phone}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default ProfilesTab;
