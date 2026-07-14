import { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';
import { API_BASE_URL as API } from '../../lib/api';
import { decodeToken, getUserId, getUserName } from '../../lib/auth';

function DashboardLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();

  // --- JWT-based user data ---
  const jwtPayload = decodeToken();
  const jwtNombre = getUserName(jwtPayload);
  const jwtId = getUserId(jwtPayload);

  const getHeaders = () => {
    const token = localStorage.getItem("token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
  };

  // --- Notification count from backend ---
  const [notificationCount, setNotificationCount] = useState(0);

  useEffect(() => {
    if (!jwtId) return;
    fetch(`${API}/api/notifications/student/${jwtId}`, { headers: getHeaders() })
      .then(res => {
        if (!res.ok) return [];
        return res.json();
      })
      .then(data => {
        const unread = Array.isArray(data) ? data.filter(n => !n.leida).length : 0;
        setNotificationCount(unread);
      })
      .catch(() => setNotificationCount(0));
  }, [jwtId]);

  const user = {
    nombre: jwtNombre
      || localStorage.getItem("userEmail")
      || 'Usuario UCE',
    rol: (jwtPayload.rol || jwtPayload.role || 'Estudiante'),
    avatarUrl: null
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userEmail");
    console.log("Logged out from DashboardLayout");
    navigate('/login');
  };

  return (
    <div className="flex h-screen w-full overflow-hidden overflow-x-hidden bg-gray-50 font-sans">
      {/* Sidebar (Responsive drawer internally) */}
      <Sidebar 
        user={user} 
        onLogout={handleLogout} 
        isOpen={sidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
      />

      {/* Main dashboard space */}
      <div className="flex-grow flex flex-col min-w-0 h-full overflow-hidden overflow-x-hidden w-full">
        {/* Header */}
        <Header 
          onMenuClick={() => setSidebarOpen(true)} 
          notificationCount={notificationCount}
        />

        {/* Scrollable content area */}
        <main className="flex-1 overflow-y-auto p-6 flex flex-col">
          {/* Main content page injected here */}
          <div className="flex-grow">
            <Outlet context={{ getHeaders, logout: handleLogout }} />
          </div>

          {/* Footer */}
          <footer className="mt-8 pt-6 border-t border-gray-200 text-center text-xs text-gray-500 flex flex-col sm:flex-row items-center justify-between space-y-2 sm:space-y-0">
            <span>Copyright 2017 - 2025 - Universidad Central del Ecuador</span>
            <div className="flex items-center space-x-4 text-base">
              <span className="cursor-pointer hover:opacity-80" title="Facebook">📘</span>
              <span className="cursor-pointer hover:opacity-80" title="Instagram">📷</span>
              <span className="cursor-pointer hover:opacity-80" title="Twitter">🐦</span>
            </div>
          </footer>
        </main>
      </div>
    </div>
  );
}

export default DashboardLayout;
