import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Evaluations() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || '';

  const token = localStorage.getItem('token');
  let payload = {};
  if (token && token.split('.').length === 3) {
    try {
      payload = JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error(e);
    }
  }

  const userId = payload.id || payload.userId || payload.sub;
  const rol = (payload.rol || payload.role || '').toString().toUpperCase();
  const isStudent = rol.includes('STUDENT') || rol.includes('ESTUDIANTE');
  const isTutorOrCoordinador = rol.includes('TUTOR') || rol.includes('COORDINADOR') || rol.includes('ADMIN');

  // --- Estados de Estudiante ---
  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState(null);

  // --- Estados de Tutor (Formulario de Registro) ---
  const [studentId, setStudentId] = useState("");
  const [proyectoId, setProyectoId] = useState("");
  const [calificacion, setCalificacion] = useState("");
  const [comentarios, setComentarios] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const cargarEvaluacionesEstudiante = () => {
    if (!userId || !isStudent) return;
    fetch(`${API}/api/evaluations/student/${userId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return [];
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

  const registrarEvaluacion = (e) => {
    e.preventDefault();
    setMensaje(null);

    const score = parseFloat(calificacion);
    if (isNaN(score) || score < 0 || score > 10) {
      setMensaje({ text: "La calificación debe estar entre 0 y 10.", type: "error" });
      return;
    }

    setSubmitting(true);

    const dataPayload = {
      estudianteId: studentId.trim(),
      proyectoId: proyectoId.trim(),
      tutorId: String(userId),
      calificacion: score,
      comentarios: comentarios.trim()
    };

    fetch(`${API}/api/evaluations`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(dataPayload)
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al registrar evaluación");
        return response.json();
      })
      .then(() => {
        setMensaje({ text: "¡Evaluación registrada exitosamente!", type: "success" });
        setStudentId("");
        setProyectoId("");
        setCalificacion("");
        setComentarios("");
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
      })
      .finally(() => {
        setSubmitting(false);
      });
  };

  useEffect(() => {
    if (userId && isStudent) {
      cargarEvaluacionesEstudiante();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const getCalificacionColor = (score) => {
    if (score >= 7.0) return 'text-green-600 border-green-200 bg-green-50';
    if (score >= 5.0) return 'text-yellow-600 border-yellow-200 bg-yellow-50';
    return 'text-red-600 border-red-200 bg-red-50';
  };

  if (!userId) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-red-500 font-medium text-lg border border-red-200 bg-red-50 px-6 py-4 rounded-xl shadow-sm">
          Sesión inválida, por favor inicia sesión nuevamente
        </p>
      </div>
    );
  }

  // --- Vista Estudiante ---
  if (isStudent) {
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
            onClick={() => { setLoading(true); cargarEvaluacionesEstudiante(); }}
          >
            🔄 Actualizar
          </button>
        </div>

        {mensaje && (
          <div className={`text-sm p-4 rounded-lg font-medium border text-left ${
            mensaje.type === "success" 
              ? "bg-green-50 text-green-700 border-green-200" 
              : "bg-red-50 text-red-700 border-red-200"
          }`}>
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
                    <span className="text-xs text-gray-300">•</span>
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

  // --- Vista Tutor / Coordinador ---
  if (isTutorOrCoordinador) {
    return (
      <div className="max-w-2xl mx-auto p-1 space-y-6 text-left">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-xl font-bold text-gray-800 border-b border-gray-100 pb-3 mb-5 flex items-center">
            <span className="mr-2">📝</span> Registrar Evaluación Académica
          </h2>

          {mensaje && (
            <div className={`text-sm p-4 rounded-lg font-medium border mb-5 ${
              mensaje.type === "success" 
                ? "bg-green-50 text-green-700 border-green-200" 
                : "bg-red-50 text-red-700 border-red-200"
            }`}>
              {mensaje.text}
            </div>
          )}

          <form onSubmit={registrarEvaluacion} className="space-y-4">
            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 mb-1">ID del Estudiante:</label>
              <input
                type="text"
                required
                value={studentId}
                onChange={(e) => setStudentId(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] w-full border border-transparent"
                placeholder="Ej. estudiante_01"
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 mb-1">ID del Proyecto:</label>
              <input
                type="text"
                required
                value={proyectoId}
                onChange={(e) => setProyectoId(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] w-full border border-transparent"
                placeholder="Ej. PROY-01"
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 mb-1">Calificación (0 - 10):</label>
              <input
                type="number"
                required
                min="0"
                max="10"
                step="0.1"
                value={calificacion}
                onChange={(e) => setCalificacion(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] w-full border border-transparent"
                placeholder="Ej. 9.5"
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 mb-1">Comentarios:</label>
              <textarea
                value={comentarios}
                onChange={(e) => setComentarios(e.target.value)}
                rows="4"
                className="bg-gray-100 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] w-full border border-transparent resize-none"
                placeholder="Ingresa la retroalimentación y comentarios sobre el desempeño del estudiante..."
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] disabled:bg-gray-300 text-white text-sm font-semibold rounded-lg py-2.5 px-6 transition shadow-sm w-full h-[42px] mt-4"
            >
              {submitting ? 'Guardando...' : 'Registrar Evaluación'}
            </button>
          </form>
        </div>
      </div>
    );
  }

  return null;
}

export default Evaluations;
