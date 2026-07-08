import { useState, useEffect } from 'react';

function LinkageTab({ getHeaders, logout }) {
  const [projects, setProjects] = useState([]);
  const [mensajeProjects, setMensajeProjects] = useState(null);

  // Form States
  const [projectName, setProjectName] = useState("");
  const [projectDesc, setProjectDesc] = useState("");
  const [institution, setInstitution] = useState("");
  const [projectStatus, setProjectStatus] = useState("PLANNED");

  const cargarProyectos = () => {
    fetch(`/api/linkage`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al conectar con Linkage-Service");
        return response.json();
      })
      .then(data => setProjects(data))
      .catch(error => setMensajeProjects({ text: "Error al cargar: " + error.message, type: "error" }));
  };

  const registrarProyecto = (e) => {
    e.preventDefault();
    setMensajeProjects(null);
    fetch(`/api/linkage`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ name: projectName, description: projectDesc, institution, status: projectStatus })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al crear proyecto");
        return response.json();
      })
      .then(() => {
        setMensajeProjects({ text: "¡Proyecto de vinculación registrado con éxito!", type: "success" });
        setProjectName("");
        setProjectDesc("");
        setInstitution("");
        setProjectStatus("PLANNED");
        cargarProyectos();
      })
      .catch(error => setMensajeProjects({ text: "Error: " + error.message, type: "error" }));
  };

  useEffect(() => {
    cargarProyectos();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="grid-layout">
      <div className="glass-card">
        <h2 className="card-title">🔗 Registrar Proyecto de Vinculación</h2>
        {mensajeProjects && mensajeProjects.type === "success" && (
          <div className="message-box success">{mensajeProjects.text}</div>
        )}
        <form onSubmit={registrarProyecto}>
          <div className="form-group">
            <label className="form-label">Nombre del Proyecto:</label>
            <input
              type="text"
              className="form-input"
              value={projectName}
              onChange={(e) => setProjectName(e.target.value)}
              placeholder="Alfabetización Digital en Comunidades Rurales"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Institución Contraparte:</label>
            <input
              type="text"
              className="form-input"
              value={institution}
              onChange={(e) => setInstitution(e.target.value)}
              placeholder="GAD Parroquial de Calderón"
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Descripción del Proyecto:</label>
            <textarea
              className="form-textarea"
              rows="3"
              value={projectDesc}
              onChange={(e) => setProjectDesc(e.target.value)}
              placeholder="Objetivos, actividades a desarrollar y impacto social esperado..."
              required
            />
          </div>
          <div className="form-group">
            <label className="form-label">Estado Inicial:</label>
            <select
              className="form-select"
              value={projectStatus}
              onChange={(e) => setProjectStatus(e.target.value)}
            >
              <option value="PLANNED">Planificado</option>
              <option value="IN_PROGRESS">En Progreso</option>
              <option value="COMPLETED">Completado</option>
            </select>
          </div>
          <button type="submit" className="btn-primary">Crear Proyecto</button>
        </form>
      </div>

      <div className="glass-card">
        <div className="refresh-section">
          <h2 className="card-title" style={{ margin: 0, border: 'none' }}>🔗 Proyectos Activos</h2>
          <button className="btn-secondary" onClick={cargarProyectos}>🔄 Actualizar</button>
        </div>
        {mensajeProjects && mensajeProjects.type === "error" && (
          <div className="message-box error">{mensajeProjects.text}</div>
        )}

        <div className="list-container">
          {projects.length === 0 ? (
            <div className="empty-state">No hay proyectos de vinculación registrados.</div>
          ) : (
            projects.map((proj) => (
              <div key={proj.id} className="list-item">
                <div className="item-header">
                  <h3 className="item-title">{proj.name}</h3>
                  <span className={`badge status-${proj.status ? proj.status.toLowerCase().replace('_', '') : 'planned'}`}>
                    {proj.status}
                  </span>
                </div>
                <div className="item-meta">🏛️ <strong>Institución:</strong> {proj.institution}</div>
                <p style={{ margin: '8px 0 0 0', fontSize: '14px', color: 'var(--text)' }}>{proj.description}</p>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default LinkageTab;
