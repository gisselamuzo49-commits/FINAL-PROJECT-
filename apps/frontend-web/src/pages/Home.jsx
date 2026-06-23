/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect } from 'react';
import { Link, useOutletContext } from 'react-router-dom';

function Home() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';

  // --- JWT-based user data ---
  const token = localStorage.getItem('token');
  let jwtPayload = {};
  if (token && token.split('.').length === 3) {
    try { jwtPayload = JSON.parse(atob(token.split('.')[1])); }
    catch (e) { console.error('JWT decode error:', e); }
  }
  const estudianteId = jwtPayload.id || jwtPayload.userId || jwtPayload.sub;
  const nombre = jwtPayload.nombre || jwtPayload.name || jwtPayload.firstName || jwtPayload.fullName || jwtPayload.username;

  // --- PANEL States ---
  const [hoursSummary, setHoursSummary] = useState(null);
  const [loadingHours, setLoadingHours] = useState(true);
  const [errorHours, setErrorHours] = useState(null);

  const [activeInternship, setActiveInternship] = useState(null);
  const [loadingInternship, setLoadingInternship] = useState(true);
  const [errorInternship, setErrorInternship] = useState(null);

  const [notifications, setNotifications] = useState([]);
  const [loadingNotifications, setLoadingNotifications] = useState(true);
  const [errorNotifications, setErrorNotifications] = useState(null);

  const [recommendedOffer, setRecommendedOffer] = useState(null);
  const [recommendedScore, setRecommendedScore] = useState(null);
  const [loadingAI, setLoadingAI] = useState(true);
  const [errorAI, setErrorAI] = useState(null);

  // --- Date formatting ---
  const today = new Date();
  const formattedDate = today.toLocaleDateString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });

  // --- API CALLS ---
  // Load Hours Summary
  const fetchHours = (id) => {
    fetch(`${API}/api/hours/student/${id}`, { headers: getHeaders() })
      .then(res => {
        if (res.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (res.status === 404) return { totalHorasValidadas: 0, totalHorasPendientes: 0 };
        if (!res.ok) throw new Error("Error al obtener horas");
        return res.json();
      })
      .then(data => {
        setHoursSummary(data);
        setLoadingHours(false);
      })
      .catch(err => {
        setErrorHours(err.message);
        setLoadingHours(false);
      });
  };

  // Load Active Internship
  const fetchActiveInternship = (id) => {
    fetch(`${API}/api/internships/applications/student/${id}`, { headers: getHeaders() })
      .then(res => {
        if (res.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (res.status === 404) return [];
        if (!res.ok) throw new Error("Error al obtener postulaciones");
        return res.json();
      })
      .then(data => {
        // Find application with state === 'ACEPTADA'
        const active = data.find(app => app.estado === 'ACEPTADA');
        setActiveInternship(active || null);
        setLoadingInternship(false);
      })
      .catch(err => {
        setErrorInternship(err.message);
        setLoadingInternship(false);
      });
  };

  // Load Notifications
  const fetchNotifications = (id) => {
    fetch(`${API}/api/notifications/student/${id}`, { headers: getHeaders() })
      .then(res => {
        if (res.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (res.status === 404) return [];
        if (!res.ok) throw new Error("Error al obtener notificaciones");
        return res.json();
      })
      .then(data => {
        // Sort: unread first, then by date desc, slice first 3
        const sorted = [...data]
          .sort((a, b) => {
            if (a.leida !== b.leida) return a.leida ? 1 : -1;
            return new Date(b.createdAt) - new Date(a.createdAt);
          })
          .slice(0, 3);
        setNotifications(sorted);
        setLoadingNotifications(false);
      })
      .catch(err => {
        setErrorNotifications(err.message);
        setLoadingNotifications(false);
      });
  };

  // Load AI Recommendation
  const fetchAIRecommendations = (id) => {
    // 1. Get all available internships
    fetch(`${API}/api/internships`, { headers: getHeaders() })
      .then(res => {
        if (res.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!res.ok) throw new Error("Error al obtener pasantías para IA");
        return res.json();
      })
      .then(internshipsList => {
        if (internshipsList.length === 0) {
          setRecommendedOffer(null);
          setLoadingAI(false);
          return;
        }

        // 2. Build recommend payload
        const carrera = "Ingeniería";
        const payload = {
          estudianteId: String(id),
          perfil: `Estudiante de la carrera de ${carrera}. Interesado en realizar mis prácticas preprofesionales desarrollando proyectos en mi área de estudio.`,
          ofertas: internshipsList.map(i => ({
            id: String(i.id),
            descripcion: `${i.title} ${i.description}`
          }))
        };

        // 3. Post to AI recommend endpoint
        return fetch(`${API}/api/ai/recommend`, {
          method: "POST",
          headers: getHeaders(),
          body: JSON.stringify(payload)
        })
          .then(res => {
            if (!res.ok) throw new Error("Error al obtener recomendaciones de IA");
            return res.json();
          })
          .then(recommendationsData => {
            const recs = recommendationsData.recomendaciones || [];
            if (recs.length === 0) {
              setRecommendedOffer(null);
              setLoadingAI(false);
              return;
            }

            // Find recommendation with highest score
            const bestRec = recs.reduce((prev, current) => (prev.score > current.score) ? prev : current);
            
            // Find corresponding offer details
            const matchedOffer = internshipsList.find(i => String(i.id) === String(bestRec.id));
            if (matchedOffer) {
              setRecommendedOffer(matchedOffer);
              setRecommendedScore(bestRec.score);
            }
            setLoadingAI(false);
          });
      })
      .catch(err => {
        setErrorAI(err.message);
        setLoadingAI(false);
      });
  };

  useEffect(() => {
    if (estudianteId) {
      fetchHours(estudianteId);
      fetchActiveInternship(estudianteId);
      fetchNotifications(estudianteId);
      fetchAIRecommendations(estudianteId);
    } else {
      // If student profile is still loading, set skeleton/wait states
      setLoadingHours(true);
      setLoadingInternship(true);
      setLoadingNotifications(true);
      setLoadingAI(true);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [estudianteId]);

  // Donut chart calculations
  const totalRequiredHours = 240;
  const hoursValidated = hoursSummary?.totalHorasValidadas || 0;
  const hoursPending = hoursSummary?.totalHorasPendientes || 0;
  const hoursPercent = Math.min(100, Math.round((hoursValidated / totalRequiredHours) * 100));
  
  // Circumference = 2 * PI * r = 2 * 3.14159 * 60 = 377
  const strokeDasharray = 377;
  const strokeDashoffset = strokeDasharray - (strokeDasharray * hoursPercent) / 100;

  return (
    <div className="flex flex-col space-y-6 p-1 text-left">
      {/* PANEL 1: Welcome Header */}
      <div className="bg-gradient-to-r from-[var(--color-navy-dark)] to-indigo-950 rounded-2xl shadow-sm border border-gray-100 p-6 text-white flex flex-col md:flex-row justify-between items-start md:items-center space-y-2 md:space-y-0">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight text-white" style={{ color: '#FFFFFF', textShadow: '0 2px 4px rgba(0,0,0,0.5)' }}>
            Bienvenido/a{nombre ? `, ${nombre}` : ''}
          </h1>
          <p className="text-gray-300 text-sm mt-1">
            Sistema Inteligente de Gestión de Pasantías y Vinculación
          </p>
        </div>
        <div className="bg-white/10 px-4 py-2 rounded-xl border border-white/10">
          <span className="text-xs text-gray-300 block font-semibold uppercase tracking-wider">Fecha de hoy</span>
          <span className="text-sm font-bold text-[var(--color-gold)] capitalize">{formattedDate}</span>
        </div>
      </div>

      {/* Main Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        
        {/* PANEL 2: Donut Chart of Hours */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center">
              <span className="mr-2">⏱️</span> Progreso de Prácticas
            </h2>

            {loadingHours ? (
              <div className="flex flex-col items-center py-10 space-y-4 animate-pulse">
                <div className="h-28 w-28 rounded-full bg-gray-200" />
                <div className="h-4 w-32 bg-gray-200 rounded" />
              </div>
            ) : errorHours ? (
              <div className="text-sm text-red-600 bg-red-50 p-4 rounded-xl border border-red-100 my-4 text-center">
                ⚠️ Error: {errorHours}
              </div>
            ) : (
              <div className="flex flex-col md:flex-row items-center justify-around py-4 gap-6">
                {/* SVG Donut */}
                <div className="relative h-40 w-40 flex items-center justify-center shrink-0">
                  <svg width="150" height="150" viewBox="0 0 150 150" className="transform -rotate-90">
                    {/* Background track */}
                    <circle cx="75" cy="75" r="60" fill="transparent" stroke="#E5E7EB" strokeWidth="12" />
                    {/* Filled track */}
                    <circle
                      cx="75"
                      cy="75"
                      r="60"
                      fill="transparent"
                      stroke="#3B82F6"
                      strokeWidth="12"
                      strokeDasharray={strokeDasharray}
                      strokeDashoffset={strokeDashoffset}
                      strokeLinecap="round"
                      className="transition-all duration-1000 ease-out"
                    />
                  </svg>
                  <div className="absolute flex flex-col items-center justify-center">
                    <span className="text-2xl font-black text-gray-800">{hoursPercent}%</span>
                    <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wide">Completado</span>
                  </div>
                </div>

                {/* Details */}
                <div className="space-y-3 w-full md:w-auto text-left">
                  <div className="border-l-4 border-blue-500 pl-3">
                    <span className="text-xs text-gray-400 block font-semibold uppercase">Validadas</span>
                    <span className="text-xl font-black text-blue-600">{hoursValidated.toFixed(1)} h</span>
                  </div>
                  <div className="border-l-4 border-yellow-500 pl-3">
                    <span className="text-xs text-gray-400 block font-semibold uppercase">Pendientes</span>
                    <span className="text-lg font-bold text-yellow-600">{hoursPending.toFixed(1)} h</span>
                  </div>
                  <div className="border-l-4 border-gray-300 pl-3">
                    <span className="text-xs text-gray-400 block font-semibold uppercase">Restantes</span>
                    <span className="text-lg font-bold text-gray-500">{Math.max(0, totalRequiredHours - hoursValidated).toFixed(1)} h</span>
                  </div>
                </div>
              </div>
            )}
          </div>
          {!loadingHours && !errorHours && (
            <div className="text-xs text-gray-500 bg-gray-50 p-3 rounded-xl border border-gray-100 text-center font-medium mt-4">
              Total requerido: {totalRequiredHours} horas de prácticas preprofesionales.
            </div>
          )}
        </div>

        {/* PANEL 3: Active Internship */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center">
              <span className="mr-2">🏢</span> Pasantía Activa
            </h2>

            {loadingInternship ? (
              <div className="space-y-4 py-8 animate-pulse text-left">
                <div className="h-6 w-3/4 bg-gray-200 rounded" />
                <div className="h-4 w-1/2 bg-gray-200 rounded" />
                <div className="h-4 w-2/3 bg-gray-200 rounded" />
              </div>
            ) : errorInternship ? (
              <div className="text-sm text-red-600 bg-red-50 p-4 rounded-xl border border-red-100 my-4 text-center">
                ⚠️ Error: {errorInternship}
              </div>
            ) : !activeInternship ? (
              <div className="flex flex-col items-center justify-center py-12 text-center text-gray-400 border-2 border-dashed border-gray-200 rounded-xl bg-gray-50/50">
                <span className="text-3xl mb-2">🔍</span>
                <p className="text-sm font-medium">No tienes pasantía activa actualmente</p>
                <Link to="/internships" className="text-xs text-[var(--color-purple)] font-semibold hover:underline mt-2">
                  Postularse a ofertas disponibles
                </Link>
              </div>
            ) : (
              <div className="space-y-4 text-left">
                <div>
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-green-100 text-green-700 border border-green-200 inline-block mb-2">
                    {activeInternship.estado}
                  </span>
                  <h3 className="text-lg font-black text-gray-900 leading-tight">
                    {activeInternship.title || "Pasantía"}
                  </h3>
                </div>
                <div className="space-y-2 text-sm text-gray-600">
                  <div className="flex items-center">
                    <span className="mr-2">🏢</span>
                    <span><strong>Empresa:</strong> {activeInternship.company}</span>
                  </div>
                  <div className="flex items-center">
                    <span className="mr-2">👨‍🏫</span>
                    <span><strong>Tutor:</strong> Asignado por la Facultad</span>
                  </div>
                  <div className="flex items-center">
                    <span className="mr-2">📅</span>
                    <span><strong>Postulado:</strong> {new Date(activeInternship.fechaPostulacion).toLocaleDateString()}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
          {!loadingInternship && activeInternship && (
            <Link to="/internships/applications" className="text-xs text-[var(--color-purple)] hover:underline font-bold text-center mt-6 block border-t border-gray-100 pt-3">
              Ver detalle de postulaciones
            </Link>
          )}
        </div>

        {/* PANEL 4: Latest Notifications */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center justify-between">
              <span className="flex items-center"><span className="mr-2">🔔</span> Notificaciones Recientes</span>
              {!loadingNotifications && notifications.length > 0 && (
                <Link to="/notifications" className="text-xs text-[var(--color-purple)] hover:underline font-semibold">
                  Ver todas →
                </Link>
              )}
            </h2>

            {loadingNotifications ? (
              <div className="space-y-4 py-4 animate-pulse">
                <div className="h-10 bg-gray-200 rounded-lg" />
                <div className="h-10 bg-gray-200 rounded-lg" />
                <div className="h-10 bg-gray-200 rounded-lg" />
              </div>
            ) : errorNotifications ? (
              <div className="text-sm text-red-600 bg-red-50 p-4 rounded-xl border border-red-100 my-4 text-center">
                ⚠️ Error: {errorNotifications}
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-10 text-center text-gray-400">
                <span className="text-2xl mb-2">📭</span>
                <p className="text-xs">No tienes notificaciones pendientes</p>
              </div>
            ) : (
              <div className="space-y-3">
                {notifications.map((notif, idx) => (
                  <div key={notif.id || idx} className={`flex items-start space-x-3 p-3 rounded-xl border transition-colors ${notif.leida ? 'bg-white border-gray-100' : 'bg-purple-50/20 border-purple-100/50'}`}>
                    <span className="text-lg shrink-0 mt-0.5">
                      {notif.tipo === 'HORAS_RECHAZADAS' ? '❌' : '✅'}
                    </span>
                    <div className="min-w-0 flex-1 text-left">
                      <p className={`text-xs text-gray-700 leading-normal truncate ${!notif.leida ? 'font-semibold' : ''}`}>
                        {notif.mensaje}
                      </p>
                      <span className="text-[10px] text-gray-400 mt-1 block">
                        {new Date(notif.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* PANEL 5: AI Recommendation */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col justify-between">
          <div>
            <h2 className="text-lg font-bold text-gray-800 border-b border-gray-100 pb-3 mb-4 flex items-center justify-between">
              <span className="flex items-center"><span className="mr-2">🎯</span> Recomendación IA Destacada</span>
              {!loadingAI && recommendedOffer && (
                <Link to="/recommendations" className="text-xs text-[var(--color-purple)] hover:underline font-semibold">
                  Ver todas →
                </Link>
              )}
            </h2>

            {loadingAI ? (
              <div className="space-y-3 py-6 animate-pulse text-left">
                <div className="h-4 w-1/4 bg-gray-200 rounded" />
                <div className="h-6 w-3/4 bg-gray-200 rounded" />
                <div className="h-4 w-1/2 bg-gray-200 rounded" />
              </div>
            ) : errorAI ? (
              <div className="text-sm text-red-600 bg-red-50 p-4 rounded-xl border border-red-100 my-4 text-center">
                ⚠️ Error: {errorAI}
              </div>
            ) : !recommendedOffer ? (
              <div className="flex flex-col items-center justify-center py-10 text-center text-gray-400 border-2 border-dashed border-gray-200 rounded-xl bg-gray-50/50">
                <span className="text-2xl mb-1">🤖</span>
                <p className="text-xs">No hay ofertas disponibles para recomendar</p>
              </div>
            ) : (
              <div className="space-y-3 text-left">
                <div className="flex items-center space-x-2">
                  <span className="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-blue-100 text-blue-700 border border-blue-200">
                    IA Recomendada
                  </span>
                  <span className="text-xs font-semibold text-blue-600">
                    {Math.round(recommendedScore * 100)}% afinidad
                  </span>
                </div>
                <div>
                  <h3 className="text-base font-bold text-gray-900 leading-tight">
                    {recommendedOffer.title}
                  </h3>
                  <span className="text-xs text-gray-500 block mt-0.5">
                    🏢 {recommendedOffer.company}
                  </span>
                </div>
                <p className="text-xs text-gray-600 line-clamp-2 leading-relaxed">
                  {recommendedOffer.description}
                </p>
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}

export default Home;
