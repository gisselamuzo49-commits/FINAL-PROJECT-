import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import selloUce from '../../assets/images/branding/sello-uce.png';

function Sidebar({ user = {}, onLogout, isOpen, onClose }) {
  const [internshipsOpen, setInternshipsOpen] = useState(false);
  const [linkageOpen, setLinkageOpen] = useState(false);

  const token = localStorage.getItem('token');
  const payload = token && token.split('.').length === 3 ? JSON.parse(atob(token.split('.')[1])) : {};
  const rol = (payload.rol 
    || payload.role 
    || payload.authorities 
    || payload.authority 
    || '').toString().toUpperCase();

  const isEstudiante = rol.includes('ESTUDIANTE');
  const isTutor = rol.includes('TUTOR');
  const isCoordinador = rol.includes('COORDINADOR') || rol.includes('ADMIN');

  const getInitials = (name) => {
    if (!name) return 'U';
    const parts = name.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0].substring(0, 2).toUpperCase();
  };

  const navLinkClass = ({ isActive }) =>
    `flex items-center px-4 py-2.5 text-sm font-medium rounded-lg transition-colors ${
      isActive
        ? 'bg-[var(--color-purple)] text-white'
        : 'text-gray-300 hover:bg-white/10 hover:text-white'
    }`;

  const subNavLinkClass = ({ isActive }) =>
    `flex items-center pl-10 pr-4 py-2 text-xs font-medium rounded-lg transition-colors ${
      isActive
        ? 'bg-[var(--color-purple)]/50 text-white'
        : 'text-gray-400 hover:bg-white/5 hover:text-white'
    }`;

  const sidebarContent = (
    <aside className="w-64 h-full bg-[var(--color-navy-dark)] border-t-4 border-[var(--color-gold)] flex flex-col text-white shadow-xl">
      {/* Top logo */}
      <div className="flex justify-center items-center py-6 border-b border-white/10">
        <img src={selloUce} alt="Sello UCE" className="h-20 w-auto object-contain" />
      </div>

      {/* Nav Menu */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1.5 scrollbar-thin scrollbar-thumb-white/10">
        {/* Home */}
        <NavLink to="/home" className={navLinkClass}>
          <span className="mr-3 text-base">🏠</span>
          <span>Home</span>
        </NavLink>

        {/* Pasantías Dropdown */}
        {(isEstudiante || isTutor || isCoordinador) && (
          <div>
            <button
              onClick={() => setInternshipsOpen(!internshipsOpen)}
              className="w-full flex items-center justify-between px-4 py-2.5 text-sm font-medium text-gray-300 rounded-lg hover:bg-white/10 hover:text-white transition-colors focus:outline-none"
            >
              <div className="flex items-center">
                <span className="mr-3 text-base">💼</span>
                <span>Pasantías</span>
              </div>
              <span className={`text-xs transition-transform duration-200 ${internshipsOpen ? 'rotate-90' : ''}`}>
                ▶
              </span>
            </button>
            {internshipsOpen && (
              <div className="mt-1 space-y-1 pl-2">
                <NavLink to="/internships" className={subNavLinkClass} end>
                  <span>Ofertas</span>
                </NavLink>
              </div>
            )}
          </div>
        )}

        {/* Mis Postulaciones */}
        {(isEstudiante || isCoordinador) && (
          <NavLink to="/internships/applications" className={navLinkClass}>
            <span className="mr-3 text-base">📋</span>
            <span>Mis Postulaciones</span>
          </NavLink>
        )}

        {/* Vinculación Dropdown */}
        {(isEstudiante || isTutor || isCoordinador) && (
          <div>
            <button
              onClick={() => setLinkageOpen(!linkageOpen)}
              className="w-full flex items-center justify-between px-4 py-2.5 text-sm font-medium text-gray-300 rounded-lg hover:bg-white/10 hover:text-white transition-colors focus:outline-none"
            >
              <div className="flex items-center">
                <span className="mr-3 text-base">🔗</span>
                <span>Vinculación</span>
              </div>
              <span className={`text-xs transition-transform duration-200 ${linkageOpen ? 'rotate-90' : ''}`}>
                ▶
              </span>
            </button>
            {linkageOpen && (
              <div className="mt-1 space-y-1 pl-2">
                <NavLink to="/linkage" className={subNavLinkClass}>
                  <span>Proyectos</span>
                </NavLink>
              </div>
            )}
          </div>
        )}

        {/* Usuarios */}
        {isCoordinador && (
          <NavLink to="/users" className={navLinkClass}>
            <span className="mr-3 text-base">👤</span>
            <span>Usuarios</span>
          </NavLink>
        )}

        {/* Horas */}
        {(isEstudiante || isTutor || isCoordinador) && (
          <NavLink to="/hours" className={navLinkClass}>
            <span className="mr-3 text-base">⏱️</span>
            <span>Horas</span>
          </NavLink>
        )}

        {/* Evaluaciones */}
        {(isEstudiante || isTutor || isCoordinador) && (
          <NavLink to="/evaluations" className={navLinkClass}>
            <span className="mr-3 text-base">📝</span>
            <span>Evaluaciones</span>
          </NavLink>
        )}

        {/* Documentos */}
        {(isEstudiante || isTutor || isCoordinador) && (
          <NavLink to="/documents" className={navLinkClass}>
            <span className="mr-3 text-base">📂</span>
            <span>Documentos</span>
          </NavLink>
        )}

        {/* Reportes */}
        {(isTutor || isCoordinador) && (
          <NavLink to="/reports" className={navLinkClass}>
            <span className="mr-3 text-base">📊</span>
            <span>Reportes</span>
          </NavLink>
        )}

        {/* Notificaciones */}
        {(isEstudiante || isTutor || isCoordinador) && (
          <NavLink to="/notifications" className={navLinkClass}>
            <span className="mr-3 text-base">🔔</span>
            <span>Notificaciones</span>
          </NavLink>
        )}

        {/* Recomendaciones */}
        {(isEstudiante || isCoordinador) && (
          <NavLink to="/recommendations" className={navLinkClass}>
            <span className="mr-3 text-base">🎯</span>
            <span>Recomendaciones</span>
          </NavLink>
        )}

        {/* Login */}
        {(!isEstudiante && !isTutor && !isCoordinador) && (
          <NavLink to="/login" className={navLinkClass}>
            <span className="mr-3 text-base">🔑</span>
            <span>Login</span>
          </NavLink>
        )}
      </nav>

      {/* Footer Profile & Logout */}
      <div className="p-4 border-t border-white/10 bg-black/20 flex items-center justify-between">
        <div className="flex items-center space-x-3 overflow-hidden">
          {user.avatarUrl ? (
            <img src={user.avatarUrl} alt={user.nombre} className="h-10 w-10 rounded-full object-cover border border-white/20" />
          ) : (
            <div className="h-10 w-10 rounded-full bg-[var(--color-gold)] text-white flex items-center justify-center font-bold text-sm border border-white/20">
              {getInitials(user.nombre)}
            </div>
          )}
          <div className="flex flex-col min-w-0">
            <span className="text-sm font-semibold truncate text-white">{user.nombre || 'Usuario UCE'}</span>
            <span className="text-xs text-gray-400 truncate">{user.rol || 'Estudiante'}</span>
          </div>
        </div>
        <button
          onClick={onLogout}
          title="Cerrar Sesión"
          className="text-gray-400 hover:text-white transition-colors p-1.5 rounded-lg hover:bg-white/10 focus:outline-none"
        >
          ⚙️
        </button>
      </div>
    </aside>
  );

  return (
    <>
      {/* Mobile Drawer (visible only on mobile) */}
      <div
        className={`fixed inset-0 z-40 lg:hidden transition-opacity duration-300 ${
          isOpen ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        }`}
      >
        {/* Dark overlay */}
        <div className="absolute inset-0 bg-black/50" onClick={onClose} />
        
        {/* Drawer content */}
        <div
          className={`absolute top-0 bottom-0 left-0 transition-transform duration-300 transform ${
            isOpen ? 'translate-x-0' : '-translate-x-full'
          }`}
        >
          {sidebarContent}
        </div>
      </div>

      {/* Desktop Sidebar (hidden on mobile, always visible on lg screens) */}
      <div className="hidden lg:block h-screen sticky top-0">
        {sidebarContent}
      </div>
    </>
  );
}

export default Sidebar;
