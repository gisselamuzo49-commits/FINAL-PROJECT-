import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';
import { API_BASE_URL as API } from '../lib/api';
import { decodeToken, getUserId } from '../lib/auth';

function Notifications() {
  const { getHeaders, logout } = useOutletContext();

  const payload = decodeToken();
  const estudianteId = getUserId(payload);

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [mensajeErr, setMensajeErr] = useState(null);

  const cargarNotificaciones = () => {
    if (!estudianteId) return;
    fetch(`${API}/api/notifications/student/${estudianteId}`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (response.status === 404) return [];
        if (!response.ok) throw new Error("Error al cargar notificaciones");
        return response.json();
      })
      .then(data => {
        // Ordenar en frontend: no leídas primero, luego por fecha descendente
        const sorted = [...data].sort((a, b) => {
          if (a.leida !== b.leida) {
            return a.leida ? 1 : -1; // false (no leída) primero
          }
          // Fecha descendente
          return new Date(b.createdAt) - new Date(a.createdAt);
        });
        setNotifications(sorted);
        setLoading(false);
      })
      .catch(error => {
        setMensajeErr("Error: " + error.message);
        setLoading(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      cargarNotificaciones();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    let wsHost = window.location.hostname;
    if (API.includes('://')) {
      wsHost = API.split('://')[1];
    }
    const wsProto = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const ws = new WebSocket(`${wsProto}://${wsHost}/ws/websocket`);

    ws.onopen = () => {
      console.log('WebSocket conectado');
      // STOMP CONNECT frame
      ws.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0');
    };

    ws.onmessage = (event) => {
      if (event.data.includes('MESSAGE')) {
        // Recargar notificaciones cuando llega una nueva
        cargarNotificaciones();
      }
    };

    ws.onerror = (error) => {
      console.warn('WebSocket error:', error);
    };

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  const marcarComoLeida = (id) => {
    fetch(`${API}/api/notifications/${id}/read`, {
      method: "PATCH",
      headers: getHeaders()
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al marcar como leída");
        return response.json();
      })
      .then(() => {
        cargarNotificaciones();
      })
      .catch(error => {
        setMensajeErr("Error: " + error.message);
      });
  };

  const getNotifIcon = (tipo) => {
    if (tipo === 'HORAS_RECHAZADAS') return '❌';
    return '✅';
  };

  const formatFecha = (dateStr) => {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      const pad = (n) => String(n).padStart(2, '0');
      const dia = pad(date.getDate());
      const mes = pad(date.getMonth() + 1);
      const anio = date.getFullYear();
      const horas = pad(date.getHours());
      const minutos = pad(date.getMinutes());
      return `${dia}/${mes}/${anio} ${horas}:${minutos}`;
    } catch {
      return dateStr;
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
      {/* Header and Refresh */}
      <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
        <h2 className="text-xl font-bold text-gray-800 flex items-center">
          <span className="mr-2">🔔</span> Mis Notificaciones
        </h2>
        <button 
          type="button"
          className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
          onClick={() => { setLoading(true); cargarNotificaciones(); }}
        >
          🔄 Actualizar
        </button>
      </div>

      {mensajeErr && (
        <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200 text-left">
          {mensajeErr}
        </div>
      )}

      {/* Notifications List */}
      <div className="space-y-3">
        {loading ? (
          <div className="text-gray-400 text-sm text-center py-10">
            Cargando notificaciones...
          </div>
        ) : notifications.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 text-center text-gray-400 text-sm">
            No tienes notificaciones todavía.
          </div>
        ) : (
          notifications.map((notif, idx) => (
            <div 
              key={notif.id || idx} 
              className={`rounded-xl border border-gray-100 p-4 transition-colors flex items-center justify-between gap-4 ${
                notif.leida 
                  ? 'bg-white' 
                  : 'bg-purple-50/20 border-purple-100/50 shadow-sm'
              }`}
            >
              <div className="flex items-center space-x-3 min-w-0 text-left">
                {/* Unread indicator dot */}
                {!notif.leida && (
                  <span className="h-2 w-2 rounded-full bg-[var(--color-purple)] shrink-0" title="No leída" />
                )}
                <div className="text-xl shrink-0">
                  {getNotifIcon(notif.tipo)}
                </div>
                <div className="min-w-0">
                  <p className={`text-sm text-gray-800 leading-relaxed ${!notif.leida ? 'font-medium' : ''}`}>
                    {notif.mensaje}
                  </p>
                  <p className="text-[10px] text-gray-400 mt-1">
                    {formatFecha(notif.createdAt)}
                  </p>
                </div>
              </div>

              {/* Mark as read button */}
              {!notif.leida && (
                <button
                  type="button"
                  onClick={() => marcarComoLeida(notif.id)}
                  className="bg-white hover:bg-gray-50 text-gray-700 text-xs font-semibold rounded-lg py-1.5 px-3 border border-gray-200 transition shrink-0 hover:text-[var(--color-purple)] hover:border-[var(--color-purple)]"
                >
                  Marcar como leída
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default Notifications;
