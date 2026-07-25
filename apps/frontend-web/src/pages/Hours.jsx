import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Hours() {
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
  const [summary, setSummary] = useState({
    totalHorasValidadas: 0.0,
    totalHorasPendientes: 0.0,
    historial: []
  });
  const [loadingSummary, setLoadingSummary] = useState(true);
  const [mensaje, setMensaje] = useState(null);

  // Form States (Registro - Estudiante)
  const [proyectoId, setProyectoId] = useState("");
  const [fecha, setFecha] = useState("");
  const [horas, setHoras] = useState("");
  const [descripcionActividad, setDescripcionActividad] = useState("");
  const [submitting, setSubmitting] = useState(false);

  // --- Estados de Tutor (Búsqueda y Aprobación) ---
  const [searchEstudianteId, setSearchEstudianteId] = useState("");
  const [searchSummary, setSearchSummary] = useState(null);
  const [loadingSearch, setLoadingSearch] = useState(false);
  const [searchError, setSearchError] = useState(null);
  const [validandoId, setValidandoId] = useState(null);

  const cargarResumenPropio = () => {
    if (!userId || !isStudent) return;
    fetch(`${API}/api/hours/student/${userId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) {
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

  const registrarHoras = (e) => {
    e.preventDefault();
    setMensaje(null);
    setSubmitting(true);

    const dataPayload = {
      estudianteId: String(userId),
      proyectoId,
      fecha,
      horas: parseFloat(horas),
      descripcionActividad,
      estado: "PENDIENTE"
    };

    fetch(`${API}/api/hours`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify(dataPayload)
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
        cargarResumenPropio();
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
      })
      .finally(() => {
        setSubmitting(false);
      });
  };

  // --- Acciones de Tutor ---
  const buscarEstudiante = (e) => {
    if (e) e.preventDefault();
    if (!searchEstudianteId.trim()) return;

    setLoadingSearch(true);
    setSearchError(null);
    setSearchSummary(null);

    fetch(`${API}/api/hours/student/${searchEstudianteId.trim()}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) {
          throw new Error("Estudiante sin registros o ID inexistente");
        }
        if (!response.ok) throw new Error("Error al consultar horas del estudiante");
        return response.json();
      })
      .then(data => {
        setSearchSummary(data);
        setLoadingSearch(false);
      })
      .catch(error => {
        setSearchError(error.message);
        setLoadingSearch(false);
      });
  };

  const validarHoras = (registroId, aprobado) => {
    setValidandoId(registroId);
    fetch(`${API}/api/hours/${registroId}/validar`, {
      method: "PATCH",
      headers: getHeaders(),
      body: JSON.stringify({ tutorId: String(userId), aprobado })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al validar registro de horas");
        return response.json();
      })
      .then(() => {
        setMensaje({ text: `Registro de horas ${aprobado ? 'aprobado' : 'rechazado'} con éxito.`, type: "success" });
        // Refrescar la búsqueda del estudiante actual
        buscarEstudiante();
      })
      .catch(error => {
        setMensaje({ text: "Error al validar: " + error.message, type: "error" });
      })
      .finally(() => {
        setValidandoId(null);
      });
  };

  useEffect(() => {
    if (userId && isStudent) {
      cargarResumenPropio();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'VALIDADO':
        return 'bg-green-100 text-green-700 border border-green-200';
      case 'RECHAZADO':
        return 'bg-red-100 text-red-700 border border-red-200';
      case 'PENDIENTE':
      default:
        return 'bg-yellow-100 text-yellow-700 border border-yellow-200';
    }
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
          <div className={`text-sm p-4 rounded-lg font-medium border text-left ${
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
                onClick={() => { setLoadingSummary(true); cargarResumenPropio(); }}
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

  // --- Vista Tutor / Coordinador ---
  if (isTutorOrCoordinador) {
    // Filtrar solo registros pendientes de la búsqueda actual
    const registrosPendientes = searchSummary
      ? (searchSummary.historial || []).filter(r => r.estado === 'PENDIENTE')
      : [];

    return (
      <div className="space-y-6">
        {/* Buscador de Estudiante */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 max-w-2xl mx-auto text-left">
          <h2 className="text-xl font-bold text-gray-800 border-b border-gray-100 pb-3 mb-5 flex items-center">
            <span className="mr-2">🔎</span> Validar Horas de Estudiantes
          </h2>

          <form onSubmit={buscarEstudiante} className="flex flex-col sm:flex-row items-end gap-4">
            <div className="flex flex-col flex-1 w-full text-left">
              <label className="text-sm font-medium text-gray-700 mb-1.5">ID del Estudiante:</label>
              <input
                type="text"
                required
                value={searchEstudianteId}
                onChange={(e) => setSearchEstudianteId(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-2.5 text-sm text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] w-full border border-transparent"
                placeholder="Ingresa el ID o código de estudiante..."
              />
            </div>
            <button
              type="submit"
              disabled={loadingSearch}
              className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white text-sm font-semibold rounded-lg py-2.5 px-6 transition shadow-sm w-full sm:w-auto h-[42px]"
            >
              {loadingSearch ? 'Buscando...' : 'Buscar'}
            </button>
          </form>
        </div>

        {mensaje && (
          <div className={`text-sm p-4 rounded-lg font-medium border max-w-4xl mx-auto text-left ${
            mensaje.type === "success" 
              ? "bg-green-50 text-green-700 border-green-200" 
              : "bg-red-50 text-red-700 border-red-200"
          }`}>
            {mensaje.text}
          </div>
        )}

        {searchError && (
          <div className="bg-red-50 text-red-700 text-sm p-4 rounded-xl border border-red-200 max-w-2xl mx-auto text-left font-medium">
            ⚠️ Error: {searchError}
          </div>
        )}

        {/* Dashboard de Progreso del Estudiante Buscado */}
        {searchSummary && (
          <div className="space-y-6">
            {/* Resumen Académico */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col sm:flex-row justify-around items-center space-y-4 sm:space-y-0 sm:space-x-6 max-w-4xl mx-auto">
              <div className="text-center">
                <div className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-1">Horas Validadas</div>
                <div className="text-3xl font-bold text-green-600">
                  {searchSummary.totalHorasValidadas.toFixed(1)}
                </div>
                <div className="text-xs text-gray-500 mt-1">Aprobadas</div>
              </div>
              <div className="hidden sm:block h-12 w-[1px] bg-gray-200" />
              <div className="text-center">
                <div className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-1">Horas Pendientes</div>
                <div className="text-3xl font-bold text-yellow-600">
                  {searchSummary.totalHorasPendientes.toFixed(1)}
                </div>
                <div className="text-xs text-gray-500 mt-1">En espera</div>
              </div>
              <div className="hidden sm:block h-12 w-[1px] bg-gray-200" />
              <div className="text-center">
                <div className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-1">Total Acumulado</div>
                <div className="text-3xl font-bold text-[var(--color-navy-dark)]">
                  {(searchSummary.totalHorasValidadas + searchSummary.totalHorasPendientes).toFixed(1)}
                </div>
                <div className="text-xs text-gray-500 mt-1">Registradas</div>
              </div>
            </div>

            {/* Listado de Registros Pendientes */}
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 max-w-4xl mx-auto text-left">
              <h3 className="text-lg font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4">
                ⌛ Registros Pendientes de Validación
              </h3>

              {registrosPendientes.length === 0 ? (
                <div className="text-gray-400 text-sm text-center py-10">
                  No hay registros pendientes de validación para este estudiante.
                </div>
              ) : (
                <div className="space-y-4">
                  {registrosPendientes.map((entry) => (
                    <div key={entry.registroId} className="border border-gray-100 rounded-xl p-5 bg-gray-50/50 hover:bg-gray-50 transition-colors flex flex-col md:flex-row md:items-start justify-between gap-4">
                      <div className="flex-1 space-y-2 text-left">
                        <div className="flex items-center space-x-3">
                          <span className="text-xs font-semibold text-gray-400 uppercase">Proyecto ID: {entry.proyectoId}</span>
                          <span className="text-xs text-gray-300">•</span>
                          <span className="text-xs font-bold text-gray-500">Fecha: {entry.fecha}</span>
                        </div>
                        <div className="text-sm font-extrabold text-[var(--color-purple)]">
                          ⏱️ {entry.horas} horas reportadas
                        </div>
                        <p className="text-sm text-gray-600 leading-relaxed bg-white border border-gray-100 rounded-lg p-3">
                          {entry.descripcionActividad}
                        </p>
                      </div>

                      {/* Botones de Aprobación */}
                      <div className="flex md:flex-col gap-2 shrink-0 md:w-32 justify-end">
                        <button
                          onClick={() => validarHoras(entry.registroId, true)}
                          disabled={validandoId === entry.registroId}
                          className="bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white text-xs font-bold py-2 px-4 rounded-lg w-full transition shadow-sm h-[36px]"
                        >
                          Aprobar
                        </button>
                        <button
                          onClick={() => validarHoras(entry.registroId, false)}
                          disabled={validandoId === entry.registroId}
                          className="bg-red-600 hover:bg-red-700 disabled:bg-gray-300 text-white text-xs font-bold py-2 px-4 rounded-lg w-full transition shadow-sm h-[36px]"
                        >
                          Rechazar
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    );
  }

  return null;
}

export default Hours;
