import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Profile() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || '';

  const token = localStorage.getItem('token');
  let jwtPayload = {};
  if (token && token.split('.').length === 3) {
    try {
      jwtPayload = JSON.parse(atob(token.split('.')[1]));
    } catch (e) {
      console.error('JWT decode error:', e);
    }
  }

  const userId = jwtPayload.id || jwtPayload.userId || jwtPayload.sub;
  const initialRole = jwtPayload.rol || jwtPayload.role || 'STUDENT';
  const userEmail = jwtPayload.email || jwtPayload.sub || '';

  // Stats states
  const [stats, setStats] = useState({
    postulaciones: 0,
    horas: 0,
    evaluaciones: 0
  });

  // Profile fields state
  const [profile, setProfile] = useState({
    id: userId,
    firstName: '',
    lastName: '',
    email: userEmail,
    phone: '',
    role: initialRole,
    carrera: '',
    facultad: '',
    habilidades: '',
    cursos: '',
    experiencia: '',
    descripcion: ''
  });

  // Original profile copy (for Cancel)
  const [originalProfile, setOriginalProfile] = useState(null);

  // Editing states
  const [isEditingPersonal, setIsEditingPersonal] = useState(false);
  const [isEditingProfessional, setIsEditingProfessional] = useState(false);

  // Status/Loading states
  const [loading, setLoading] = useState(userId ? true : false);
  const [saving, setSaving] = useState(false);
  const [mensaje, setMensaje] = useState(null);

  // Load profile and stats on mount
  useEffect(() => {
    if (!userId) return;

    // 1. Fetch Profile
    fetch(`${API}/api/users/profile/${userId}`, { headers: getHeaders() })
      .then(res => {
        if (res.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (res.status === 404) {
          // If profile does not exist yet, we will initialize with basic JWT data
          const newProfile = {
            id: userId,
            firstName: jwtPayload.nombre || '',
            lastName: '',
            email: userEmail,
            phone: '',
            role: initialRole,
            carrera: '',
            facultad: '',
            habilidades: '',
            cursos: '',
            experiencia: '',
            descripcion: ''
          };
          // Try to create it on backend so it exists
          return fetch(`${API}/api/users`, {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(newProfile)
          }).then(createRes => createRes.ok ? newProfile : newProfile);
        }
        if (!res.ok) throw new Error("Error al obtener perfil");
        return res.json();
      })
      .then(data => {
        setProfile(data);
        setOriginalProfile(data);
      })
      .catch(err => {
        setMensaje({ text: err.message, type: "error" });
      })
      .finally(() => {
        setLoading(false);
      });

    // 2. Fetch Stats
    // Postulaciones
    fetch(`${API}/api/internships/applications/student/${userId}`, { headers: getHeaders() })
      .then(res => res.ok ? res.json() : [])
      .then(apps => {
        setStats(prev => ({ ...prev, postulaciones: apps.length }));
      })
      .catch(() => {});

    // Horas
    fetch(`${API}/api/hours/student/${userId}`, { headers: getHeaders() })
      .then(res => res.ok ? res.json() : { totalHorasValidadas: 0 })
      .then(hoursData => {
        setStats(prev => ({ ...prev, horas: hoursData.totalHorasValidadas || 0 }));
      })
      .catch(() => {});

    // Evaluaciones
    fetch(`${API}/api/evaluations/student/${userId}`, { headers: getHeaders() })
      .then(res => res.ok ? res.json() : [])
      .then(evals => {
        setStats(prev => ({ ...prev, evaluaciones: evals.length }));
      })
      .catch(() => {});

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const saveChanges = (section) => {
    setSaving(true);
    setMensaje(null);

    // Build update payload
    const payload = {
      firstName: profile.firstName,
      lastName: profile.lastName,
      phone: profile.phone,
      carrera: profile.carrera,
      facultad: profile.facultad,
      habilidades: profile.habilidades,
      cursos: profile.cursos,
      experiencia: profile.experiencia,
      descripcion: profile.descripcion
    };

    fetch(`${API}/api/users/profile/${userId}`, {
      method: 'PUT',
      headers: getHeaders(),
      body: JSON.stringify(payload)
    })
      .then(res => {
        if (res.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!res.ok) throw new Error("Error al guardar perfil");
        return res.json();
      })
      .then(data => {
        setProfile(data);
        setOriginalProfile(data);
        setMensaje({ text: "¡Perfil actualizado con éxito!", type: "success" });
        if (section === 'personal') setIsEditingPersonal(false);
        if (section === 'professional') setIsEditingProfessional(false);
      })
      .catch(err => {
        setMensaje({ text: err.message, type: "error" });
      })
      .finally(() => {
        setSaving(false);
      });
  };

  const cancelChanges = (section) => {
    if (originalProfile) {
      setProfile(originalProfile);
    }
    if (section === 'personal') setIsEditingPersonal(false);
    if (section === 'professional') setIsEditingProfessional(false);
  };

  const deleteAccount = () => {
    const confirmDelete = window.confirm("¿Estás completamente seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer y borrará todos tus datos.");
    if (!confirmDelete) return;

    fetch(`${API}/api/users/profile/${userId}`, {
      method: 'DELETE',
      headers: getHeaders()
    })
      .then(res => {
        if (!res.ok) throw new Error("Error al eliminar cuenta");
        logout();
      })
      .catch(err => {
        setMensaje({ text: err.message, type: "error" });
      });
  };

  const getAvatarInitials = () => {
    const first = profile.firstName ? profile.firstName.trim().charAt(0) : '';
    const last = profile.lastName ? profile.lastName.trim().charAt(0) : '';
    return (first + last).toUpperCase() || 'U';
  };

  const getRoleBadgeClass = () => {
    const r = profile.role || '';
    if (r.includes('STUDENT') || r.includes('ESTUDIANTE')) return 'bg-blue-100 text-blue-800';
    if (r.includes('TUTOR')) return 'bg-green-100 text-green-800';
    return 'bg-purple-100 text-purple-800';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <p className="text-gray-500 font-medium text-lg animate-pulse">Cargando perfil...</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-4 space-y-6 text-left">
      {/* Header */}
      <div className="border-b border-gray-100 pb-3 mb-5 flex justify-between items-center">
        <div>
          <h2 className="text-xl font-bold text-gray-800 flex items-center">
            <span className="mr-2">👤</span> Mi Perfil de Usuario
          </h2>
          <p className="text-xs text-gray-500 mt-1">
            Gestiona tu información académica y profesional para el sistema de pasantías.
          </p>
        </div>
      </div>

      {mensaje && (
        <div className={`text-sm p-4 rounded-xl font-medium border ${
          mensaje.type === "success" 
            ? "bg-green-50 text-green-700 border-green-200" 
            : "bg-red-50 text-red-700 border-red-200"
        }`}>
          {mensaje.text}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        
        {/* LEFT COLUMN: Info Personal & Avatar */}
        <div className="md:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-6">
            <div className="flex justify-between items-center border-b border-gray-50 pb-3">
              <h3 className="font-bold text-gray-800 flex items-center">
                <span className="mr-2 text-navy-dark">📋</span> Información Personal
              </h3>
              {!isEditingPersonal ? (
                <button
                  onClick={() => setIsEditingPersonal(true)}
                  className="text-xs font-semibold text-[var(--color-purple)] hover:text-[var(--color-purple-hover)]"
                >
                  Editar
                </button>
              ) : (
                <div className="space-x-2">
                  <button
                    disabled={saving}
                    onClick={() => saveChanges('personal')}
                    className="text-xs font-bold text-green-600 hover:text-green-700 disabled:text-gray-400"
                  >
                    Guardar
                  </button>
                  <button
                    onClick={() => cancelChanges('personal')}
                    className="text-xs font-semibold text-gray-500 hover:text-gray-600"
                  >
                    Cancelar
                  </button>
                </div>
              )}
            </div>

            <div className="flex flex-col sm:flex-row items-center gap-6">
              {/* UCE Navy Avatar */}
              <div 
                className="w-20 h-20 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-md select-none"
                style={{ backgroundColor: '#0F1B3C' }}
              >
                {getAvatarInitials()}
              </div>

              <div className="flex-1 grid grid-cols-1 sm:grid-cols-2 gap-4 w-full">
                <div>
                  <label className="text-xs font-semibold text-gray-500 block mb-1">Nombre</label>
                  {isEditingPersonal ? (
                    <input
                      type="text"
                      name="firstName"
                      value={profile.firstName || ''}
                      onChange={handleInputChange}
                      className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    />
                  ) : (
                    <p className="text-sm font-semibold text-gray-800">{profile.firstName || '—'}</p>
                  )}
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-500 block mb-1">Apellido</label>
                  {isEditingPersonal ? (
                    <input
                      type="text"
                      name="lastName"
                      value={profile.lastName || ''}
                      onChange={handleInputChange}
                      className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    />
                  ) : (
                    <p className="text-sm font-semibold text-gray-800">{profile.lastName || '—'}</p>
                  )}
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-500 block mb-1">Email (Institucional)</label>
                  <p className="text-sm font-semibold text-gray-700 bg-gray-50 px-3 py-2 rounded-lg border border-gray-100">
                    {profile.email}
                  </p>
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-500 block mb-1">Teléfono</label>
                  {isEditingPersonal ? (
                    <input
                      type="text"
                      name="phone"
                      value={profile.phone || ''}
                      onChange={handleInputChange}
                      className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    />
                  ) : (
                    <p className="text-sm font-semibold text-gray-800">{profile.phone || '—'}</p>
                  )}
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-500 block mb-1">Carrera</label>
                  {isEditingPersonal ? (
                    <input
                      type="text"
                      name="carrera"
                      value={profile.carrera || ''}
                      onChange={handleInputChange}
                      className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    />
                  ) : (
                    <p className="text-sm font-semibold text-gray-800">{profile.carrera || '—'}</p>
                  )}
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-500 block mb-1">Facultad</label>
                  {isEditingPersonal ? (
                    <input
                      type="text"
                      name="facultad"
                      value={profile.facultad || ''}
                      onChange={handleInputChange}
                      className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                    />
                  ) : (
                    <p className="text-sm font-semibold text-gray-800">{profile.facultad || '—'}</p>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* SECTION 2: Perfil Profesional */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-6">
            <div className="flex justify-between items-center border-b border-gray-50 pb-3">
              <h3 className="font-bold text-gray-800 flex items-center">
                <span className="mr-2 text-navy-dark">💼</span> Perfil Profesional
              </h3>
              {!isEditingProfessional ? (
                <button
                  onClick={() => setIsEditingProfessional(true)}
                  className="text-xs font-semibold text-[var(--color-purple)] hover:text-[var(--color-purple-hover)]"
                >
                  Editar
                </button>
              ) : (
                <div className="space-x-2">
                  <button
                    disabled={saving}
                    onClick={() => saveChanges('professional')}
                    className="text-xs font-bold text-green-600 hover:text-green-700 disabled:text-gray-400"
                  >
                    Guardar
                  </button>
                  <button
                    onClick={() => cancelChanges('professional')}
                    className="text-xs font-semibold text-gray-500 hover:text-gray-600"
                  >
                    Cancelar
                  </button>
                </div>
              )}
            </div>

            <div className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-gray-500 block mb-1">Descripción Personal</label>
                {isEditingProfessional ? (
                  <textarea
                    name="descripcion"
                    rows="3"
                    value={profile.descripcion || ''}
                    onChange={handleInputChange}
                    placeholder="Soy estudiante de..."
                    className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                  />
                ) : (
                  <p className="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-xl p-3 border border-gray-100 whitespace-pre-line">
                    {profile.descripcion || 'Sin descripción redactada.'}
                  </p>
                )}
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 block mb-1">Habilidades Técnicas</label>
                {isEditingProfessional ? (
                  <textarea
                    name="habilidades"
                    rows="2"
                    value={profile.habilidades || ''}
                    onChange={handleInputChange}
                    placeholder="Java, Python, React, AWS, Docker..."
                    className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                  />
                ) : (
                  <p className="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-xl p-3 border border-gray-100">
                    {profile.habilidades || 'Habilidades no listadas.'}
                  </p>
                )}
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 block mb-1">Cursos y Certificaciones</label>
                {isEditingProfessional ? (
                  <textarea
                    name="cursos"
                    rows="2"
                    value={profile.cursos || ''}
                    onChange={handleInputChange}
                    placeholder="AWS Cloud Practitioner, SCRUM Master..."
                    className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                  />
                ) : (
                  <p className="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-xl p-3 border border-gray-100">
                    {profile.cursos || 'Cursos no listados.'}
                  </p>
                )}
              </div>

              <div>
                <label className="text-xs font-semibold text-gray-500 block mb-1">Experiencia Laboral</label>
                {isEditingProfessional ? (
                  <textarea
                    name="experiencia"
                    rows="3"
                    value={profile.experiencia || ''}
                    onChange={handleInputChange}
                    placeholder="Desarrollador Junior en Empresa X (2023-2024)..."
                    className="bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 w-full text-sm text-gray-850 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] resize-none"
                  />
                ) : (
                  <p className="text-sm text-gray-700 leading-relaxed bg-gray-50 rounded-xl p-3 border border-gray-100 whitespace-pre-line">
                    {profile.experiencia || 'Sin experiencia registrada.'}
                  </p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: Role, Stats & Actions */}
        <div className="space-y-6">
          {/* Role badge card */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center space-y-4">
            <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider">Rol de Acceso</h4>
            <span className={`inline-block px-3 py-1 rounded-full text-xs font-bold ${getRoleBadgeClass()}`}>
              {profile.role || 'ESTUDIANTE'}
            </span>
          </div>

          {/* SECTION 3: Estadísticas */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 space-y-4">
            <h3 className="font-bold text-gray-800 border-b border-gray-50 pb-2 flex items-center">
              <span className="mr-2">📊</span> Actividad
            </h3>

            <div className="space-y-4">
              <div className="flex justify-between items-center bg-gray-50/50 p-3 rounded-xl border border-gray-50">
                <span className="text-xs font-semibold text-gray-500">Postulaciones</span>
                <span className="text-lg font-bold text-[#0F1B3C]">{stats.postulaciones}</span>
              </div>

              <div className="flex justify-between items-center bg-gray-50/50 p-3 rounded-xl border border-gray-50">
                <span className="text-xs font-semibold text-gray-500">Horas validadas</span>
                <span className="text-lg font-bold text-[#0F1B3C]">{stats.horas} hrs</span>
              </div>

              <div className="flex justify-between items-center bg-gray-50/50 p-3 rounded-xl border border-gray-50">
                <span className="text-xs font-semibold text-gray-500">Evaluaciones</span>
                <span className="text-lg font-bold text-[#0F1B3C]">{stats.evaluaciones}</span>
              </div>
            </div>
          </div>

          {/* Danger zone / Delete Account */}
          <div className="bg-red-50/50 rounded-2xl shadow-sm border border-red-100 p-6 space-y-4">
            <h4 className="font-bold text-red-800 flex items-center text-sm">
              <span className="mr-2">⚠️</span> Zona de Peligro
            </h4>
            <p className="text-xs text-red-600">
              Eliminar tu cuenta borrará permanentemente todo tu historial del sistema de pasantías.
            </p>
            <button
              onClick={deleteAccount}
              className="bg-red-600 hover:bg-red-700 text-white font-bold text-xs py-2.5 px-4 rounded-xl w-full transition shadow-sm"
            >
              Eliminar Cuenta
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}

export default Profile;
