import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Recommendations() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';

  const token = localStorage.getItem('token');
  let payload = {};
  if (token && token.split('.').length === 3) {
    try {
      payload = JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error(e);
    }
  }
  const estudianteId = payload.id 
    || payload.userId 
    || payload.sub 
    || payload.studentId;

  const [internships, setInternships] = useState([]);
  const [loadingInternships, setLoadingInternships] = useState(true);
  const [perfilTexto, setPerfilTexto] = useState("Estudiante interesado en realizar mis prácticas preprofesionales desarrollando proyectos en mi área de estudio.");
  
  // Recommender states
  const [recommendations, setRecommendations] = useState([]);
  const [analyzing, setAnalyzing] = useState(false);
  const [mensaje, setMensaje] = useState(null);

  // Load internships on mount
  const cargarPasantias = () => {
    fetch(`${API}/api/internships`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al cargar las ofertas de pasantía");
        return response.json();
      })
      .then(data => {
        setInternships(data);
        setLoadingInternships(false);
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
        setLoadingInternships(false);
      });
  };

  useEffect(() => {
    cargarPasantias();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);



  const obtenerRecomendaciones = (e) => {
    e.preventDefault();
    if (!perfilTexto.trim()) {
      setMensaje({ text: "Por favor, escribe los detalles de tu perfil antes de buscar.", type: "error" });
      return;
    }

    setAnalyzing(true);
    setMensaje(null);
    setRecommendations([]);

    const payload = {
      estudianteId: String(estudianteId),
      perfil: perfilTexto,
      ofertas: internships.map(i => ({
        id: String(i.id),
        descripcion: `${i.title} ${i.description}`
      }))
    };

    fetch(`${API}/api/ai/recommend`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(payload)
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al analizar el perfil de recomendaciones");
        return response.json();
      })
      .then(data => {
        setRecommendations(data.recomendaciones || []);
        if ((data.recomendaciones || []).length === 0) {
          setMensaje({ text: "No se encontraron coincidencias. Intenta ampliar la descripción de tu perfil.", type: "info" });
        }
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
      })
      .finally(() => {
        setAnalyzing(false);
      });
  };

  if (!estudianteId) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-red-500 font-medium text-lg border border-red-200 bg-red-50 px-6 py-4 rounded-xl shadow-sm">
          Sesión inválida, por favor inicia sesión nuevamente
        </p>
      </div>
    );
  }

  if (loadingInternships) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-gray-500 font-medium text-lg animate-pulse">Cargando perfil...</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-1 space-y-6">
      {/* Header */}
      <div className="border-b border-gray-100 pb-3 mb-5 text-left">
        <h2 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="mr-2">🎯</span> Recomendaciones de Pasantías (Inteligencia Artificial)
        </h2>
        <p className="text-xs text-gray-500 mt-1">
          Nuestra IA analiza la afinidad de tu perfil con las ofertas de pasantías disponibles utilizando procesamiento de lenguaje natural.
        </p>
      </div>

      {mensaje && (
        <div className={`text-sm p-4 rounded-lg font-medium border text-left ${
          mensaje.type === "success" 
            ? "bg-green-50 text-green-700 border-green-200" 
            : mensaje.type === "info"
            ? "bg-blue-50 text-blue-700 border-blue-200"
            : "bg-red-50 text-red-700 border-red-200"
        }`}>
          {mensaje.text}
        </div>
      )}

      {internships.length === 0 ? (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center text-gray-400 text-sm">
          No hay ofertas disponibles para analizar todavía.
        </div>
      ) : (
        <div className="space-y-6">
          {/* Form / Profile Input */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-left">
            <form onSubmit={obtenerRecomendaciones} className="space-y-4">
              <div className="flex flex-col">
                <label className="text-sm font-semibold text-gray-700 block mb-1">
                  Describe tus habilidades, tecnologías de interés y metas:
                </label>
                <textarea
                  required
                  rows="5"
                  value={perfilTexto}
                  onChange={(e) => setPerfilTexto(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                  placeholder="Ej: Desarrollador con experiencia en React, bases de datos SQL e interés en proyectos de impacto social..."
                />
              </div>

              <button
                type="submit"
                disabled={analyzing}
                className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] disabled:bg-gray-400 text-white font-semibold rounded-lg py-3 px-4 w-full transition shadow-sm flex items-center justify-center space-x-2"
              >
                {analyzing ? (
                  <span className="animate-pulse">Analizando perfil...</span>
                ) : (
                  <>
                    <span>🔍</span>
                    <span>Buscar Recomendaciones</span>
                  </>
                )}
              </button>
            </form>
          </div>

          {/* Recommendations list */}
          {recommendations.length > 0 && (
            <div className="space-y-4">
              <h3 className="text-base font-bold text-gray-800 border-b border-gray-100 pb-2 text-left">
                🎯 Pasantías Recomendadas para Ti
              </h3>
              
              <div className="space-y-3">
                {recommendations.map((rec, idx) => {
                  const offer = internships.find(i => String(i.id) === String(rec.id));
                  if (!offer) return null;

                  const matchPercentage = Math.round(rec.score * 100);

                  return (
                    <div key={rec.id || idx} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col md:flex-row md:items-center justify-between gap-4 text-left">
                      <div className="flex-1 space-y-2">
                        <h4 className="text-base font-bold text-gray-900 leading-tight">
                          {offer.title}
                        </h4>
                        <div className="text-xs text-gray-500">
                          🏢 <strong>Empresa:</strong> {offer.company}
                        </div>
                        <p className="text-sm text-gray-600 leading-relaxed line-clamp-3">
                          {offer.description}
                        </p>
                      </div>

                      {/* Percentage Circle / Bar */}
                      <div className="flex flex-col items-center justify-center shrink-0 w-28">
                        <div className="text-2xl font-extrabold text-[var(--color-purple)]">
                          {matchPercentage}%
                        </div>
                        <div className="text-[10px] font-bold text-gray-400 uppercase tracking-wider mt-0.5">
                          Coincidencia
                        </div>
                        {/* Tiny custom progress bar */}
                        <div className="w-full bg-gray-100 rounded-full h-1.5 mt-2 overflow-hidden border border-gray-200/50">
                          <div 
                            className="bg-[var(--color-purple)] h-full rounded-full transition-all duration-500"
                            style={{ width: `${matchPercentage}%` }}
                          />
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default Recommendations;
