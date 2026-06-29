import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Internships() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}`;
  const [pasantias, setPasantias] = useState([]);
  const [mensajePasantias, setMensajePasantias] = useState(null);

  // --- Decodificar Rol desde JWT ---
  const token = localStorage.getItem('token');
  const payload = token && token.split('.').length === 3 ? JSON.parse(atob(token.split('.')[1])) : {};
  const rol = (payload.rol || payload.role || '').toString().toUpperCase();
  const estudianteId = payload.id || payload.userId || payload.sub;
  
  const canPublish = rol.includes('TUTOR') || rol.includes('COORDINADOR') || rol.includes('ADMIN');
  const isStudent = rol.includes('STUDENT') || rol.includes('ESTUDIANTE');

  // Form States (Tutor)
  const [title, setTitle] = useState("");
  const [company, setCompany] = useState("");
  const [description, setDescription] = useState("");
  const [statusPasantia, setStatusPasantia] = useState("ABIERTA");

  // Postulaciones (Estudiante)
  const [postuladasIds, setPostuladasIds] = useState([]);

  // Aplicantes (Tutor/Coordinador)
  const [selectedInternshipId, setSelectedInternshipId] = useState(null);
  const [aplicantes, setAplicantes] = useState([]);
  const [cargandoAplicantes, setCargandoAplicantes] = useState(false);

  const cargarPasantias = () => {
    fetch(`${API}/api/internships`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al conectar con Internship-Service");
        return response.json();
      })
      .then(data => setPasantias(data))
      .catch(error => setMensajePasantias({ text: "Error al cargar: " + error.message, type: "error" }));
  };

  const cargarPostulacionesEstudiante = () => {
    if (!estudianteId || !isStudent) return;
    fetch(`${API}/api/internships/applications/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return [];
        if (!response.ok) throw new Error("Error al cargar postulaciones del estudiante");
        return response.json();
      })
      .then(data => {
        const ids = data.map(app => app.internshipId);
        setPostuladasIds(ids);
      })
      .catch(error => console.error("Error al cargar postulaciones previas:", error));
  };

  const registrarPasantia = (e) => {
    e.preventDefault();
    setMensajePasantias(null);
    fetch(`${API}/api/internships`, {
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

  const postularse = (internshipId) => {
    setMensajePasantias(null);
    fetch(`${API}/api/internships/${internshipId}/applications`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ estudianteId: String(estudianteId), mensaje: "Solicitud de postulación" })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al postularse a la oferta");
        return response.json();
      })
      .then(() => {
        setMensajePasantias({ text: "¡Postulación enviada exitosamente!", type: "success" });
        cargarPostulacionesEstudiante();
      })
      .catch(error => setMensajePasantias({ text: "Error: " + error.message, type: "error" }));
  };

  const cargarAplicantes = (internshipId) => {
    setSelectedInternshipId(internshipId);
    setCargandoAplicantes(true);
    fetch(`${API}/api/internships/${internshipId}/applications`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return [];
        if (!response.ok) throw new Error("Error al cargar aplicantes");
        return response.json();
      })
      .then(data => {
        setAplicantes(data);
        setCargandoAplicantes(false);
      })
      .catch(error => {
        console.error("Error al cargar aplicantes:", error);
        setCargandoAplicantes(false);
      });
  };

  const actualizarEstadoPostulacion = (postulacionId, nuevoEstado) => {
    setMensajePasantias(null);
    fetch(`${API}/api/internships/applications/${postulacionId}/status`, {
      method: "PATCH",
      headers: getHeaders(),
      body: JSON.stringify({ estado: nuevoEstado })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al actualizar estado");
        return response.json();
      })
      .then(() => {
        setMensajePasantias({ text: `Postulación ${nuevoEstado === 'ACEPTADA' ? 'aceptada' : 'rechazada'} exitosamente.`, type: "success" });
        if (selectedInternshipId) {
          cargarAplicantes(selectedInternshipId);
        }
      })
      .catch(error => setMensajePasantias({ text: "Error: " + error.message, type: "error" }));
  };

  useEffect(() => {
    cargarPasantias();
    if (isStudent) {
      cargarPostulacionesEstudiante();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-6">
      <div className={`grid grid-cols-1 ${canPublish ? 'lg:grid-cols-2' : 'max-w-3xl mx-auto'} gap-6 p-1`}>
        {/* Create Internship Card */}
        {canPublish && (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col">
            <h2 className="text-xl font-bold text-gray-800 border-b border-gray-100 pb-3 mb-5 flex items-center">
              <span className="mr-2">💼</span> Publicar Oferta de Pasantía
            </h2>
            
            {mensajePasantias && mensajePasantias.type === "success" && !selectedInternshipId && (
              <div className="bg-green-50 text-green-700 text-sm p-4 rounded-lg mb-5 font-medium border border-green-200">
                {mensajePasantias.text}
              </div>
            )}

            <form onSubmit={registrarPasantia} className="space-y-4 flex-1 flex flex-col justify-between">
              <div className="space-y-4">
                <div className="flex flex-col">
                  <label className="text-sm font-medium text-gray-700 text-left block mb-1">Título del Puesto:</label>
                  <input
                    type="text"
                    required
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    placeholder="Desarrollador Java Fullstack"
                  />
                </div>
                
                <div className="flex flex-col">
                  <label className="text-sm font-medium text-gray-700 text-left block mb-1">Empresa / Institución:</label>
                  <input
                    type="text"
                    required
                    value={company}
                    onChange={(e) => setCompany(e.target.value)}
                    className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    placeholder="Corporación Financiera Nacional"
                  />
                </div>

                <div className="flex flex-col">
                  <label className="text-sm font-medium text-gray-700 text-left block mb-1">Descripción de Actividades:</label>
                  <textarea
                    required
                    rows="3"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                    placeholder="Detalles sobre las tareas a realizar y requisitos del estudiante..."
                  />
                </div>

                <div className="flex flex-col">
                  <label className="text-sm font-medium text-gray-700 text-left block mb-1">Estado de la Oferta:</label>
                  <select
                    value={statusPasantia}
                    onChange={(e) => setStatusPasantia(e.target.value)}
                    className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] appearance-none cursor-pointer"
                  >
                    <option value="ABIERTA">Abierta</option>
                    <option value="CERRADA">Cerrada</option>
                  </select>
                </div>
              </div>

              <button 
                type="submit" 
                className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white font-semibold rounded-lg py-3 px-4 w-full transition shadow-sm mt-6"
              >
                Publicar Oferta
              </button>
            </form>
          </div>
        )}

        {/* Available Internships Card */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col h-[600px] lg:h-auto">
          <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
            <h2 className="text-xl font-bold text-gray-800 flex items-center">
              <span className="mr-2">💼</span> Ofertas Disponibles
            </h2>
            <button 
              className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
              onClick={cargarPasantias}
            >
              🔄 Actualizar
            </button>
          </div>

          {mensajePasantias && mensajePasantias.type === "error" && (
            <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200">
              {mensajePasantias.text}
            </div>
          )}

          {mensajePasantias && mensajePasantias.type === "success" && (selectedInternshipId || isStudent) && (
            <div className="bg-green-50 text-green-700 text-sm p-4 rounded-lg mb-5 font-medium border border-green-200">
              {mensajePasantias.text}
            </div>
          )}

          <div className="flex-1 overflow-y-auto space-y-3 pr-1 scrollbar-thin">
            {pasantias.length === 0 ? (
              <div className="text-gray-400 text-sm text-center py-10">
                No hay pasantías publicadas. ¡Crea la primera!
              </div>
            ) : (
              pasantias.map((p) => {
                const yaPostulado = postuladasIds.includes(p.id);
                return (
                  <div key={p.id} className="border border-gray-100 rounded-xl p-4 bg-gray-50/50 hover:bg-gray-50 transition-colors shadow-sm">
                    <div className="flex justify-between items-start mb-2">
                      <h3 className="text-base font-bold text-gray-900 leading-tight text-left">{p.title}</h3>
                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        p.status === 'ABIERTA' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
                      }`}>
                        {p.status}
                      </span>
                    </div>
                    <div className="text-xs text-gray-500 mb-2 text-left">
                      🏢 <strong>Empresa:</strong> {p.company}
                    </div>
                    <p className="text-sm text-gray-600 leading-relaxed text-left line-clamp-3 mb-4">
                      {p.description}
                    </p>

                    {/* Botón de Postularse para Estudiantes */}
                    {isStudent && (
                      <button
                        onClick={() => postularse(p.id)}
                        disabled={yaPostulado || p.status !== 'ABIERTA'}
                        className={`text-sm font-semibold py-2 px-4 rounded-lg w-full transition shadow-sm ${
                          yaPostulado
                            ? 'bg-gray-100 text-gray-400 border border-gray-200 cursor-not-allowed'
                            : p.status !== 'ABIERTA'
                            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                            : 'bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white'
                        }`}
                      >
                        {yaPostulado ? 'Ya postulado' : 'Postularse'}
                      </button>
                    )}

                    {/* Botón de Ver Aplicantes para Tutores */}
                    {canPublish && (
                      <button
                        onClick={() => cargarAplicantes(p.id)}
                        className={`text-sm font-semibold py-2 px-4 rounded-lg w-full transition border ${
                          selectedInternshipId === p.id
                            ? 'bg-[var(--color-purple)] text-white border-transparent'
                            : 'bg-white hover:bg-gray-50 text-gray-700 border-gray-200'
                        }`}
                      >
                        {selectedInternshipId === p.id ? 'Viendo Aplicantes' : 'Ver Aplicantes'}
                      </button>
                    )}
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>

      {/* Postulaciones Recibidas Panel (Tutor/Coordinador) */}
      {canPublish && selectedInternshipId && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 max-w-6xl mx-auto text-left">
          <h3 className="text-lg font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center justify-between">
            <span>📋 Postulaciones Recibidas (Oferta #{selectedInternshipId})</span>
            <button 
              onClick={() => setSelectedInternshipId(null)}
              className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-500 px-2.5 py-1 rounded-lg border border-gray-200 font-semibold transition"
            >
              Cerrar Panel
            </button>
          </h3>

          {cargandoAplicantes ? (
            <div className="text-gray-400 text-sm text-center py-10 animate-pulse">
              Cargando postulaciones...
            </div>
          ) : aplicantes.length === 0 ? (
            <div className="text-gray-400 text-sm text-center py-10">
              No hay postulaciones registradas para esta oferta.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {aplicantes.map((ap) => (
                <div key={ap.postulacionId} className="border border-gray-100 rounded-xl p-4 bg-gray-50/50 hover:bg-gray-50 transition-colors flex flex-col justify-between">
                  <div className="space-y-2 mb-4">
                    <div className="flex justify-between items-center">
                      <span className="text-xs font-semibold text-gray-400">Estudiante ID: {ap.estudianteId}</span>
                      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                        ap.estado === 'ACEPTADA'
                          ? 'bg-green-100 text-green-700'
                          : ap.estado === 'RECHAZADA'
                          ? 'bg-red-100 text-red-700'
                          : 'bg-yellow-100 text-yellow-700'
                      }`}>
                        {ap.estado}
                      </span>
                    </div>
                    <h4 className="text-base font-bold text-gray-900">{ap.nombreEstudiante || 'Estudiante UCE'}</h4>
                    {ap.mensaje && (
                      <p className="text-sm text-gray-600 bg-white border border-gray-100 rounded-lg p-3">
                        {ap.mensaje}
                      </p>
                    )}
                  </div>

                  {ap.estado === 'PENDIENTE' && (
                    <div className="flex space-x-2">
                      <button
                        onClick={() => actualizarEstadoPostulacion(ap.postulacionId, 'ACEPTADA')}
                        className="bg-green-600 hover:bg-green-700 text-white text-xs font-bold py-2 px-4 rounded-lg flex-1 transition shadow-sm"
                      >
                        Aceptar
                      </button>
                      <button
                        onClick={() => actualizarEstadoPostulacion(ap.postulacionId, 'RECHAZADA')}
                        className="bg-red-600 hover:bg-red-700 text-white text-xs font-bold py-2 px-4 rounded-lg flex-1 transition shadow-sm"
                      >
                        Rechazar
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Internships;
