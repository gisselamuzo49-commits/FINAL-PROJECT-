import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Linkage() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';
  const [projects, setProjects] = useState([]);
  const [mensajeProjects, setMensajeProjects] = useState(null);

  // Form States
  const [projectName, setProjectName] = useState("");
  const [projectDesc, setProjectDesc] = useState("");
  const [institution, setInstitution] = useState("");
  const [projectStatus, setProjectStatus] = useState("PLANNED");

  const cargarProyectos = () => {
    fetch(`${API}/api/linkage`, { headers: getHeaders() })
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
    fetch(`${API}/api/linkage`, {
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

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-700';
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-700';
      case 'PLANNED':
      default:
        return 'bg-blue-100 text-blue-700';
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 p-1">
      {/* Create Project Card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col">
        <h2 className="text-xl font-bold text-gray-800 border-b border-gray-100 pb-3 mb-5 flex items-center">
          <span className="mr-2">🔗</span> Registrar Proyecto de Vinculación
        </h2>
        
        {mensajeProjects && mensajeProjects.type === "success" && (
          <div className="bg-green-50 text-green-700 text-sm p-4 rounded-lg mb-5 font-medium border border-green-200">
            {mensajeProjects.text}
          </div>
        )}

        <form onSubmit={registrarProyecto} className="space-y-4 flex-1 flex flex-col justify-between">
          <div className="space-y-4">
            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Nombre del Proyecto:</label>
              <input
                type="text"
                required
                value={projectName}
                onChange={(e) => setProjectName(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="Alfabetización Digital en Comunidades Rurales"
              />
            </div>
            
            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Institución Contraparte:</label>
              <input
                type="text"
                required
                value={institution}
                onChange={(e) => setInstitution(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="GAD Parroquial de Calderón"
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Descripción del Proyecto:</label>
              <textarea
                required
                rows="3"
                value={projectDesc}
                onChange={(e) => setProjectDesc(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                placeholder="Objetivos, actividades a desarrollar y impacto social esperado..."
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Estado Inicial:</label>
              <select
                value={projectStatus}
                onChange={(e) => setProjectStatus(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] appearance-none cursor-pointer"
              >
                <option value="PLANNED">Planificado</option>
                <option value="IN_PROGRESS">En Progreso</option>
                <option value="COMPLETED">Completado</option>
              </select>
            </div>
          </div>

          <button 
            type="submit" 
            className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white font-semibold rounded-lg py-3 px-4 w-full transition shadow-sm mt-6"
          >
            Crear Proyecto
          </button>
        </form>
      </div>

      {/* Active Projects Card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col h-[600px] lg:h-auto">
        <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
          <h2 className="text-xl font-bold text-gray-800 flex items-center">
            <span className="mr-2">🔗</span> Proyectos Activos
          </h2>
          <button 
            className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
            onClick={cargarProyectos}
          >
            🔄 Actualizar
          </button>
        </div>

        {mensajeProjects && mensajeProjects.type === "error" && (
          <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200">
            {mensajeProjects.text}
          </div>
        )}

        <div className="flex-1 overflow-y-auto space-y-3 pr-1 scrollbar-thin">
          {projects.length === 0 ? (
            <div className="text-gray-400 text-sm text-center py-10">
              No hay proyectos de vinculación registrados.
            </div>
          ) : (
            projects.map((proj) => (
              <div key={proj.id} className="border border-gray-100 rounded-xl p-4 bg-gray-50/50 hover:bg-gray-50 transition-colors shadow-sm">
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-base font-bold text-gray-900 leading-tight text-left">{proj.name}</h3>
                  <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${getStatusBadgeClass(proj.status)}`}>
                    {proj.status}
                  </span>
                </div>
                <div className="text-xs text-gray-500 mb-2 text-left">
                  🏛️ <strong>Institución:</strong> {proj.institution}
                </div>
                <p className="text-sm text-gray-600 leading-relaxed text-left line-clamp-3">
                  {proj.description}
                </p>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default Linkage;
