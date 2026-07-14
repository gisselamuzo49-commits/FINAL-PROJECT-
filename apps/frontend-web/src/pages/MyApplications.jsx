import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { API_BASE_URL as API } from '../lib/api';
import { decodeToken, getUserId } from '../lib/auth';

function MyApplications() {
  const { getHeaders, logout } = useOutletContext();

  const payload = decodeToken();
  const estudianteId = getUserId(payload);

  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensaje, setMensaje] = useState(null);

  const cargarPostulaciones = () => {
    if (!estudianteId) return;
    fetch(`${API}/api/internships/applications/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return [];
        if (!response.ok) throw new Error("Error al cargar postulaciones");
        return response.json();
      })
      .then(data => {
        setApplications(data);
        setLoading(false);
      })
      .catch(error => {
        setMensaje({ text: "Error: " + error.message, type: "error" });
        setLoading(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      cargarPostulaciones();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  const getEstadoBadge = (estado) => {
    switch (estado) {
      case 'PENDIENTE':
        return 'bg-yellow-100 text-yellow-700 border-yellow-200';
      case 'ACEPTADA':
        return 'bg-green-100 text-green-700 border-green-200';
      case 'RECHAZADA':
        return 'bg-red-100 text-red-700 border-red-200';
      default:
        return 'bg-gray-100 text-gray-600 border-gray-200';
    }
  };

  const getEstadoIcon = (estado) => {
    switch (estado) {
      case 'PENDIENTE': return '⏳';
      case 'ACEPTADA': return '✅';
      case 'RECHAZADA': return '❌';
      default: return '📋';
    }
  };

  const formatFecha = (fechaStr) => {
    if (!fechaStr) return '—';
    try {
      const fecha = new Date(fechaStr);
      return fecha.toLocaleDateString('es-EC', {
        day: '2-digit', month: '2-digit', year: 'numeric'
      }) + ' ' + fecha.toLocaleTimeString('es-EC', {
        hour: '2-digit', minute: '2-digit'
      });
    } catch {
      return fechaStr;
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
    <div className="max-w-3xl mx-auto p-1 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
        <h2 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="mr-2">📋</span> Mis Postulaciones
        </h2>
        <button
          type="button"
          className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200"
          onClick={() => { setLoading(true); cargarPostulaciones(); }}
        >
          🔄 Actualizar
        </button>
      </div>

      {mensaje && (
        <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200">
          {mensaje.text}
        </div>
      )}

      {/* Applications List */}
      <div className="space-y-4">
        {loading ? (
          <div className="text-gray-400 text-sm text-center py-10">
            Cargando postulaciones...
          </div>
        ) : applications.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center text-gray-400 text-sm">
            Aún no te has postulado a ninguna oferta.
          </div>
        ) : (
          applications.map((app) => (
            <div key={app.postulacionId} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col md:flex-row md:items-start md:justify-between gap-4">
              <div className="flex-1 space-y-3 text-left">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="text-xs font-semibold text-gray-500 uppercase">Oferta #{app.internshipId}</span>
                  <span className="text-xs text-gray-400">•</span>
                  <span className="text-xs text-gray-500">Postulado: {formatFecha(app.fechaPostulacion)}</span>
                </div>

                <h3 className="text-base font-bold text-gray-900 leading-tight">
                  {app.title || 'Oferta de pasantía'}
                </h3>

                <div className="text-sm text-gray-600">
                  🏢 <strong>Empresa:</strong> {app.company || '—'}
                </div>

                {app.mensaje && (
                  <div className="text-sm text-gray-600 leading-relaxed bg-gray-50/50 rounded-xl p-4 border border-gray-100">
                    <span className="font-semibold text-gray-700 block mb-1">Tu mensaje:</span>
                    {app.mensaje}
                  </div>
                )}
              </div>

              {/* Estado badge */}
              <div className={`flex flex-col items-center justify-center border rounded-2xl px-5 py-4 w-32 h-20 shrink-0 ${getEstadoBadge(app.estado)}`}>
                <span className="text-xl mb-1">{getEstadoIcon(app.estado)}</span>
                <span className="text-xs font-bold uppercase tracking-wider">{app.estado}</span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default MyApplications;
