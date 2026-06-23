import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Hours() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';

  const token = localStorage.getItem('token');
  let payload = {};
  if (token && token.split('.').length === 3) {
    try {
      payload = JSON.parse(atob(token.split('.')[1]));
      console.log('JWT payload completo:', JSON.stringify(payload));
    } catch (e) {
      console.error(e);
    }
  }
  const estudianteId = payload.id 
    || payload.userId 
    || payload.sub 
    || payload.studentId;

  const [summary, setSummary] = useState({
    totalHorasValidadas: 0.0,
    totalHorasPendientes: 0.0,
    historial: []
  });
  const [loadingSummary, setLoadingSummary] = useState(true);
  const [mensaje, setMensaje] = useState(null);

  // Form States
  const [proyectoId, setProyectoId] = useState("");
  const [fecha, setFecha] = useState("");
  const [horas, setHoras] = useState("");
  const [descripcionActividad, setDescripcionActividad] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const cargarResumen = () => {
    if (!estudianteId) return;
    fetch(`${API}/api/hours/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) {
          // Tratar como "sin horas registradas todavía"
          return {
            totalHorasValidadas: 0.0,
            totalHorasPendientes: 0.0,
            historial: []
          };
        }
        if (!response.ok) throw new Error("Error al cargar el resumen de horas");
        return response.json();
      })
      .then(data => {
        setSummary(data);
        setLoadingSummary(false);
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
        setLoadingSummary(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      cargarResumen();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  const registrarHoras = (e) => {
    e.preventDefault();
    setMensaje(null);
    setSubmitting(true);

    const payload = {
      estudianteId: String(estudianteId),
      proyectoId,
      fecha,
      horas: parseFloat(horas),
      descripcionActividad,
      estado: "PENDIENTE"
    };

    fetch(`${API}/api/hours`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(payload)
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al registrar horas");
        return response.json();
      })
      .then(() => {
        setMensaje({ text: "¡Registro de horas enviado con éxito!", type: "success" });
        setProyectoId("");
        setFecha("");
        setHoras("");
        setDescripcionActividad("");
        cargarResumen();
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
      })
      .finally(() => {
        setSubmitting(false);
      });
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'VALIDADO':
        return 'bg-green-100 text-green-700';
      case 'RECHAZADO':
        return 'bg-red-100 text-red-700';
      case 'PENDIENTE':
      default:
        return 'bg-yellow-100 text-yellow-700';
    }
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

  return (
    <div className="flex flex-col space-y-6 p-1">
      {/* Resumen Superior */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col sm:flex-row justify-around items-center space-y-4 sm:space-y-0 sm:space-x-6">
        <div className="text-center">
          <div className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-1">Horas Validadas</div>
          <div className="text-3xl font-bold text-green-600">
            {loadingSummary ? '...' : summary.totalHorasValidadas.toFixed(1)}
          </div>
          <div className="text-xs text-gray-500 mt-1">Aprobadas por tu tutor</div>
        </div>
        <div className="hidden sm:block h-12 w-[1px] bg-gray-200" />
        <div className="text-center">
          <div className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-1">Horas Pendientes</div>
          <div className="text-3xl font-bold text-yellow-600">
            {loadingSummary ? '...' : summary.totalHorasPendientes.toFixed(1)}
          </div>
          <div className="text-xs text-gray-500 mt-1">En espera de validación</div>
        </div>
        <div className="hidden sm:block h-12 w-[1px] bg-gray-200" />
        <div className="text-center">
          <div className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-1">Total Acumulado</div>
          <div className="text-3xl font-bold text-[var(--color-navy-dark)]">
            {loadingSummary ? '...' : (summary.totalHorasValidadas + summary.totalHorasPendientes).toFixed(1)}
          </div>
          <div className="text-xs text-gray-500 mt-1">Suma total de horas</div>
        </div>
      </div>

      {mensaje && (
        <div className={`text-sm p-4 rounded-lg font-medium border ${
          mensaje.type === "success" 
            ? "bg-green-50 text-green-700 border-green-200" 
            : "bg-red-50 text-red-700 border-red-200"
        }`}>
          {mensaje.text}
        </div>
      )}

      {/* Main Grid: Form & History */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Formulario de registro */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col">
          <h2 className="text-xl font-bold text-gray-800 border-b border-gray-100 pb-3 mb-5 flex items-center">
            <span className="mr-2">⏱️</span> Registrar Horas
          </h2>

          <form onSubmit={registrarHoras} className="space-y-4 flex-1 flex flex-col justify-between">
            <div className="space-y-4">
              <div className="flex flex-col">
                <label className="text-sm font-medium text-gray-700 text-left block mb-1">ID del Proyecto:</label>
                <input
                  type="text"
                  required
                  value={proyectoId}
                  onChange={(e) => setProyectoId(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                  placeholder="ID del Proyecto o Convenio de Vinculación"
                />
              </div>

              <div className="flex flex-col">
                <label className="text-sm font-medium text-gray-700 text-left block mb-1">Fecha de la Actividad:</label>
                <input
                  type="date"
                  required
                  value={fecha}
                  onChange={(e) => setFecha(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                />
              </div>

              <div className="flex flex-col">
                <label className="text-sm font-medium text-gray-700 text-left block mb-1">Número de Horas:</label>
                <input
                  type="number"
                  required
                  min="0.5"
                  step="0.5"
                  value={horas}
                  onChange={(e) => setHoras(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                  placeholder="Ej. 4.5"
                />
              </div>

              <div className="flex flex-col">
                <label className="text-sm font-medium text-gray-700 text-left block mb-1">Descripción de la Actividad:</label>
                <textarea
                  required
                  rows="4"
                  value={descripcionActividad}
                  onChange={(e) => setDescripcionActividad(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                  placeholder="Describe detalladamente las tareas ejecutadas en este periodo..."
                />
              </div>
            </div>

            <button 
              type="submit" 
              disabled={submitting}
              className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] disabled:bg-gray-400 text-white font-semibold rounded-lg py-3 px-4 w-full transition shadow-sm mt-6"
            >
              {submitting ? 'Enviando...' : 'Registrar Actividad'}
            </button>
          </form>
        </div>

        {/* Historial de registro */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col h-[550px] lg:h-auto">
          <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
            <h2 className="text-xl font-bold text-gray-800 flex items-center">
              <span className="mr-2">📅</span> Historial de Actividades
            </h2>
            <button 
              type="button"
              className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
              onClick={() => { setLoadingSummary(true); cargarResumen(); }}
            >
              🔄 Actualizar
            </button>
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 pr-1 scrollbar-thin">
            {loadingSummary ? (
              <div className="text-gray-400 text-sm text-center py-10">
                Cargando historial...
              </div>
            ) : summary.historial.length === 0 ? (
              <div className="text-gray-400 text-sm text-center py-10">
                No tienes actividades registradas todavía.
              </div>
            ) : (
              summary.historial.map((entry, idx) => (
                <div key={entry.registroId || idx} className="border border-gray-100 rounded-xl p-4 bg-gray-50/50 hover:bg-gray-50 transition-colors shadow-sm text-left">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <span className="text-xs font-semibold text-gray-500 uppercase">Proyecto ID: {entry.proyectoId}</span>
                      <h3 className="text-sm font-bold text-gray-900 mt-0.5">📅 {entry.fecha}</h3>
                    </div>
                    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${getStatusBadgeClass(entry.estado)}`}>
                      {entry.estado}
                    </span>
                  </div>
                  <div className="text-sm font-semibold text-[var(--color-purple)] mb-2">
                    ⏱️ {entry.horas} horas registradas
                  </div>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    {entry.descripcionActividad}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Hours;
