import { useState, useEffect } from 'react';
import { useOutletContext } from 'react-router-dom';

function Users() {
  const { getHeaders, logout } = useOutletContext();
  const API = import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}:8082`;
  const [profiles, setProfiles] = useState([]);
  const [mensajeProfiles, setMensajeProfiles] = useState(null);

  // Form States
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [profileEmail, setProfileEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [role, setRole] = useState("STUDENT");

  const cargarPerfiles = () => {
    fetch(`${API}/api/users`, { headers: getHeaders() })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al conectar con User-Service");
        return response.json();
      })
      .then(data => setProfiles(data))
      .catch(error => setMensajeProfiles({ text: "Error al cargar: " + error.message, type: "error" }));
  };

  const registrarPerfil = (e) => {
    e.preventDefault();
    setMensajeProfiles(null);
    fetch(`${API}/api/users`, {
      method: "POST",
      headers: getHeaders(),
      body: JSON.stringify({ firstName, lastName, email: profileEmail, phone, role })
    })
      .then(response => {
        if (response.status === 401) { logout(); throw new Error("Sesión expirada"); }
        if (!response.ok) throw new Error("Error al crear perfil de usuario");
        return response.json();
      })
      .then(() => {
        setMensajeProfiles({ text: "¡Perfil de estudiante/tutor creado exitosamente!", type: "success" });
        setFirstName("");
        setLastName("");
        setProfileEmail("");
        setPhone("");
        setRole("STUDENT");
        cargarPerfiles();
      })
      .catch(error => setMensajeProfiles({ text: "Error: " + error.message, type: "error" }));
  };

  useEffect(() => {
    cargarPerfiles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const getRoleBadgeClass = (roleVal) => {
    switch (roleVal) {
      case 'COORDINATOR':
        return 'bg-purple-100 text-purple-700';
      case 'TUTOR':
        return 'bg-indigo-100 text-indigo-700';
      case 'STUDENT':
      default:
        return 'bg-blue-100 text-blue-700';
    }
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 p-1">
      {/* Create User Profile Card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col">
        <h2 className="text-xl font-bold text-gray-800 border-b border-gray-100 pb-3 mb-5 flex items-center">
          <span className="mr-2">👤</span> Crear Perfil Académico
        </h2>
        
        {mensajeProfiles && mensajeProfiles.type === "success" && (
          <div className="bg-green-50 text-green-700 text-sm p-4 rounded-lg mb-5 font-medium border border-green-200">
            {mensajeProfiles.text}
          </div>
        )}

        <form onSubmit={registrarPerfil} className="space-y-4 flex-1 flex flex-col justify-between">
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col">
                <label className="text-sm font-medium text-gray-700 text-left block mb-1">Nombre:</label>
                <input
                  type="text"
                  required
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                  placeholder="Juan"
                />
              </div>
              <div className="flex flex-col">
                <label className="text-sm font-medium text-gray-700 text-left block mb-1">Apellido:</label>
                <input
                  type="text"
                  required
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                  placeholder="Pérez"
                />
              </div>
            </div>
            
            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Correo Institucional:</label>
              <input
                type="email"
                required
                value={profileEmail}
                onChange={(e) => setProfileEmail(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="juan.perez@uce.edu.ec"
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Teléfono de Contacto:</label>
              <input
                type="tel"
                required
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="0998765432"
              />
            </div>

            <div className="flex flex-col">
              <label className="text-sm font-medium text-gray-700 text-left block mb-1">Rol en el Sistema:</label>
              <select
                value={role}
                onChange={(e) => setRole(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] appearance-none cursor-pointer"
              >
                <option value="STUDENT">Estudiante</option>
                <option value="TUTOR">Tutor Académico</option>
                <option value="COORDINATOR">Coordinador de Carrera</option>
              </select>
            </div>
          </div>

          <button 
            type="submit" 
            className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white font-semibold rounded-lg py-3 px-4 w-full transition shadow-sm mt-6"
          >
            Guardar Perfil
          </button>
        </form>
      </div>

      {/* Registered Profiles Card */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 flex flex-col h-[600px] lg:h-auto">
        <div className="flex justify-between items-center border-b border-gray-100 pb-3 mb-5">
          <h2 className="text-xl font-bold text-gray-800 flex items-center">
            <span className="mr-2">👤</span> Perfiles Registrados
          </h2>
          <button 
            className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-700 px-3 py-1.5 rounded-lg transition-colors font-medium border border-gray-200" 
            onClick={cargarPerfiles}
          >
            🔄 Actualizar
          </button>
        </div>

        {mensajeProfiles && mensajeProfiles.type === "error" && (
          <div className="bg-red-50 text-red-700 text-sm p-4 rounded-lg mb-5 font-medium border border-red-200">
            {mensajeProfiles.text}
          </div>
        )}

        <div className="flex-1 overflow-y-auto space-y-3 pr-1 scrollbar-thin">
          {profiles.length === 0 ? (
            <div className="text-gray-400 text-sm text-center py-10">
              No hay perfiles de usuarios registrados.
            </div>
          ) : (
            profiles.map((prof) => (
              <div key={prof.id} className="border border-gray-100 rounded-xl p-4 bg-gray-50/50 hover:bg-gray-50 transition-colors shadow-sm text-left">
                <div className="flex justify-between items-start mb-2">
                  <h3 className="text-base font-bold text-gray-900 leading-tight">{prof.firstName} {prof.lastName}</h3>
                  <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${getRoleBadgeClass(prof.role)}`}>
                    {prof.role}
                  </span>
                </div>
                <div className="text-xs text-gray-500 mb-1">
                  📧 <strong>Email:</strong> {prof.email}
                </div>
                <div className="text-xs text-gray-500">
                  📞 <strong>Teléfono:</strong> {prof.phone}
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default Users;
