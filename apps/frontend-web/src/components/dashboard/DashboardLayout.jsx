import { useState, useEffect } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from './Sidebar';
import Header from './Header';

function DashboardLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();

  const API_URL = `http://${window.location.hostname}`;
  const GATEWAY_PORT = import.meta.env.VITE_GATEWAY_PORT || "8082";

  const getHeaders = () => {
    const token = localStorage.getItem("token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
  };

  const [userProfile, setUserProfile] = useState(null);
  const userEmail = localStorage.getItem("userEmail");

  useEffect(() => {
    if (!userEmail) return;
    fetch(`${API_URL}:${GATEWAY_PORT}/api/users/email/${userEmail}`, { headers: getHeaders() })
      .then(res => res.ok ? res.json() : null)
      .then(data => setUserProfile(data))
      .catch(() => setUserProfile(null));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userEmail]);

  const user = {
    nombre: userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : (localStorage.getItem("userEmail") || "Estudiante UCE"),
    rol: userProfile?.role || "Estudiante Académico",
    avatarUrl: null,
    userProfile
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userEmail");
    console.log("Logged out from DashboardLayout");
    navigate('/login');
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-gray-50 font-sans">
      {/* Sidebar (Responsive drawer internally) */}
      <Sidebar 
        user={user} 
        onLogout={handleLogout} 
        isOpen={sidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
      />

      {/* Main dashboard space */}
      <div className="flex-grow flex flex-col min-w-0 h-full overflow-hidden">
        {/* Header */}
        <Header 
          onMenuClick={() => setSidebarOpen(true)} 
          notificationCount={3} // Mock notification count
        />

        {/* Scrollable content area */}
        <main className="flex-1 overflow-y-auto p-6 flex flex-col">
          {/* Main content page injected here */}
          <div className="flex-grow">
            <Outlet context={{ API_URL, GATEWAY_PORT, getHeaders, logout: handleLogout, userProfile, estudianteId: userProfile?.id }} />
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
