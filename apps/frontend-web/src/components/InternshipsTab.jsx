import { useState, useEffect } from 'react';

function InternshipsTab({ getHeaders, logout }) {
  const [pasantias, setPasantias] = useState([]);
  const [mensajePasantias, setMensajePasantias] = useState(null);
  
  // Form States
  const [title, setTitle] = useState("");
  const [company, setCompany] = useState("");
  const [description, setDescription] = useState("");
  const [statusPasantia, setStatusPasantia] = useState("ABIERTA");

  const cargarPasantias = () => {
    fetch(`/api/internships`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al conectar con Internship-Service");
        return response.json();
      })
      .then(data => setPasantias(data))
      .catch(error => setMensajePasantias({ text: "Error al cargar: " + error.message, type: "error" }));
  };

  const registrarPasantia = (e) => {
    e.preventDefault();
    setMensajePasantias(null);
    fetch(`/api/internships`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ title, company, description, status: statusPasantia })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al guardar pasantía");
        return response.json();
      })
      .then(() => {
        setMensajePasantias({ text: "¡Pasantía publicada exitosamente!", type: "success" });
        setTitle("");
        setCompany("");
        setDescription("");
        setStatusPasantia("ABIERTA");
        cargarPasantias();
      })
      .catch(error => setMensajePasantias({ text: "Error: " + error.message, type: "error" }));
  };

  useEffect(() => {
    cargarPasantias();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="grid-layout">
      <div className="glass-card">
        <h2 className="card-title">💼 Publicar Oferta de Pasantía</h2>
        {mensajePasantias && mensajePasantias.type === "success" && (
          <div className="message-box success">{mensajePasantias.text}</div>
        )}
        <form onSubmit={registrarPasantia}>
          <div className="form-group">
            <label className="form-label">Título del Puesto:</label>
            <input
              type="text"
              className="form-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Desarrollador Java Fullstack"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Empresa / Institución:</label>
            <input
              type="text"
              className="form-input"
              value={company}
              onChange={(e) => setCompany(e.target.value)}
              placeholder="Corporación Financiera Nacional"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Descripción de Actividades:</label>
            <textarea
              className="form-textarea"
              rows="3"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detalles sobre las tareas a realizar y requisitos del estudiante..."
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Estado de la Oferta:</label>
            <select
              className="form-select"
              value={statusPasantia}
              onChange={(e) => setStatusPasantia(e.target.value)}
            >
              <option value="ABIERTA">Abierta</option>
              <option value="CERRADA">Cerrada</option>
            </select>
          </div>
          <button type="submit" className="btn-primary">Publicar Oferta</button>
        </form>
      </div>

      <div className="glass-card">
        <div className="refresh-section">
          <h2 className="card-title" style={{ margin: 0, border: 'none' }}>💼 Ofertas Disponibles</h2>
          <button className="btn-secondary" onClick={cargarPasantias}>🔄 Actualizar</button>
        </div>
        {mensajePasantias && mensajePasantias.type === "error" && (
          <div className="message-box error">{mensajePasantias.text}</div>
        )}

        <div className="list-container">
          {pasantias.length === 0 ? (
            <div className="empty-state">No hay pasantías publicadas. ¡Crea la primera!</div>
          ) : (
            pasantias.map((p) => (
              <div key={p.id} className="list-item">
                 <div className="item-header">
                  <h3 className="item-title">{p.title}</h3>
                  <span className={`badge ${p.status === 'ABIERTA' ? 'status-open' : 'status-closed'}`}>
                    {p.status}
                  </span>
                </div>
                <div className="item-meta">🏢 <strong>Empresa:</strong> {p.company}</div>
                <p style={{ margin: '8px 0 0 0', fontSize: '14px', color: 'var(--text)' }}>{p.description}</p>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default InternshipsTab;
