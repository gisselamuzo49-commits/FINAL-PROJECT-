import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Evaluations() {
  const { getHeaders, logout, estudianteId } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';

  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState(null);

  const cargarEvaluaciones = () => {
    if (!estudianteId) return;
    fetch(`${API}/api/evaluations/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) {
          return [];
        }
        if (!response.ok) throw new Error("Error al cargar evaluaciones");
        return response.json();
      })
      .then(data => {
        setEvaluations(data);
        setLoading(false);
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
        setLoading(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      cargarEvaluaciones();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  const getCalificacionColor = (score) => {
    if (score >= 7.0) return 'text-green-600 border-green-200 bg-green-50';
    if (score >= 5.0) return 'text-yellow-600 border-yellow-200 bg-yellow-50';
    return 'text-red-600 border-red-200 bg-red-50';
  };

  if (!estudianteId) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-gray-500 font-medium text-lg animate-pulse">Cargando perfil...</p>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto p-1 space-y-6">
      {/* Header and Refresh */}
      <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
        <h2 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="mr-2">📝</span> Mis Evaluaciones
        </h2>
        <button 
          type="button"
          className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
          onClick={() => { setLoading(true); cargarEvaluaciones(); }}
        >
          🔄 Actualizar
        </button>
      </div>

      {mensaje && (
        <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200">
          {mensaje.text}
        </div>
      )}

      {/* Evaluations List */}
      <div className="space-y-4">
        {loading ? (
          <div className="text-gray-400 text-sm text-center py-10">
            Cargando evaluaciones...
          </div>
        ) : evaluations.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center text-gray-400 text-sm">
            Aún no tienes evaluaciones registradas.
          </div>
        ) : (
          evaluations.map((evalu) => (
            <div key={evalu.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col md:flex-row md:items-start md:justify-between gap-4">
              <div className="flex-1 space-y-3 text-left">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="text-xs font-semibold text-gray-500 uppercase">Proyecto ID: {evalu.proyectoId}</span>
                  <span className="text-xs text-gray-400">•</span>
                  <span className="text-xs text-gray-500">Fecha: {evalu.fechaEvaluacion}</span>
                </div>
                
                <div className="text-sm font-bold text-gray-800">
                  👨‍🏫 Evaluador: <span className="font-semibold text-gray-600">{evalu.nombre || "Tutor"}</span>
                </div>

                <div className="text-sm text-gray-600 leading-relaxed bg-gray-50/50 rounded-xl p-4 border border-gray-100">
                  <span className="font-semibold text-gray-700 block mb-1">Comentarios del tutor:</span>
                  {evalu.comentarios || "Sin comentarios."}
                </div>
              </div>

              {/* Rating display badge */}
              <div className={`flex flex-col items-center justify-center border rounded-2xl px-5 py-4 w-28 h-24 shrink-0 ${getCalificacionColor(evalu.calificacion)}`}>
                <span className="text-2xl font-bold tracking-tight">{evalu.calificacion}</span>
                <span className="text-[10px] font-bold uppercase tracking-wider mt-1">Nota / 10</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Evaluations;
