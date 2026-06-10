/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect } from 'react';
import './App.css';
import LoginCard from './components/LoginCard';
import DashboardHeader from './components/DashboardHeader';
import TabsNavigation from './components/TabsNavigation';
import InternshipsTab from './components/InternshipsTab';
import ProfilesTab from './components/ProfilesTab';
import LinkageTab from './components/LinkageTab';

function App() {
  const API_URL = `http://${window.location.hostname}`;
  const GATEWAY_PORT = import.meta.env.VITE_GATEWAY_PORT || "8082";

  // --- AUTHENTICATION STATE ---
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [userEmail, setUserEmail] = useState(localStorage.getItem("userEmail") || "");

  // --- GENERAL STATE ---
  const [activeTab, setActiveTab] = useState("internships");
  const [healthStatus, setHealthStatus] = useState({
    auth: "checking",
    internships: "checking",
    users: "checking",
    linkage: "checking"
  });

  // Headers with Auth Token
  const getHeaders = () => {
    const headers = { "Content-Type": "application/json" };
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
  };

  // Logout / Cerrar Sesión
  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userEmail");
    setToken(null);
    setUserEmail("");
  };

  // Verificar la salud de los servicios a través del Gateway
  const checkHealth = () => {
    setHealthStatus({
      auth: "checking",
      internships: "checking",
      users: "checking",
      linkage: "checking"
    });

    fetch(`${API_URL}:${GATEWAY_PORT}/api/auth/hello`)
      .then(res => setHealthStatus(prev => ({ ...prev, auth: res.ok ? "up" : "down" })))
      .catch(() => setHealthStatus(prev => ({ ...prev, auth: "down" })));

    fetch(`${API_URL}:${GATEWAY_PORT}/api/internships`, { headers: getHeaders() })
      .then(res => {
        if (res.status === 401) return setHealthStatus(prev => ({ ...prev, internships: "up" }));
        setHealthStatus(prev => ({ ...prev, internships: res.ok ? "up" : "down" }));
      })
      .catch(() => setHealthStatus(prev => ({ ...prev, internships: "down" })));

    fetch(`${API_URL}:${GATEWAY_PORT}/api/users/health`)
      .then(res => setHealthStatus(prev => ({ ...prev, users: res.ok ? "up" : "down" })))
      .catch(() => setHealthStatus(prev => ({ ...prev, users: "down" })));

    fetch(`${API_URL}:${GATEWAY_PORT}/api/linkage/health`)
      .then(res => setHealthStatus(prev => ({ ...prev, linkage: res.ok ? "up" : "down" })))
      .catch(() => setHealthStatus(prev => ({ ...prev, linkage: "down" })));
  };

  useEffect(() => {
    checkHealth();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  if (!token) {
    return (
      <LoginCard
        API_URL={API_URL}
        GATEWAY_PORT={GATEWAY_PORT}
        setToken={setToken}
        setUserEmail={setUserEmail}
      />
    );
  }

  return (
    <div className="dashboard-container">
      <DashboardHeader
        userEmail={userEmail}
        GATEWAY_PORT={GATEWAY_PORT}
        healthStatus={healthStatus}
        checkHealth={checkHealth}
        logout={logout}
      />

      <TabsNavigation
        activeTab={activeTab}
        setActiveTab={setActiveTab}
      />

      <main className="tab-content">
        {activeTab === 'internships' && (
          <InternshipsTab
            API_URL={API_URL}
            GATEWAY_PORT={GATEWAY_PORT}
            getHeaders={getHeaders}
            logout={logout}
          />
        )}

        {activeTab === 'profiles' && (
          <ProfilesTab
            API_URL={API_URL}
            GATEWAY_PORT={GATEWAY_PORT}
            getHeaders={getHeaders}
            logout={logout}
          />
        )}

        {activeTab === 'linkage' && (
          <LinkageTab
            API_URL={API_URL}
            GATEWAY_PORT={GATEWAY_PORT}
            getHeaders={getHeaders}
            logout={logout}
          />
        )}
      </main>
    </div>
  );
}

export default App;