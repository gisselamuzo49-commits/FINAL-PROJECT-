function DashboardHeader({ userEmail, GATEWAY_PORT, healthStatus, checkHealth, logout }) {
  return (
    <header className="dashboard-header">
      <div className="header-title-section">
        <h1>UCE Vinculación y Pasantías 🚀</h1>
        <p>
          Sesión: <code>{userEmail}</code> | Gateway Port: <code>{GATEWAY_PORT}</code>
        </p>
      </div>

      <div className="system-status-panel">
        <div className="status-badge" title="Routed via Gateway">
          <span className={`status-dot ${healthStatus.auth}`}></span>
          Auth: {healthStatus.auth.toUpperCase()}
        </div>
        <div className="status-badge" title="Routed via Gateway">
          <span className={`status-dot ${healthStatus.internships}`}></span>
          Internships: {healthStatus.internships.toUpperCase()}
        </div>
        <div className="status-badge" title="Routed via Gateway">
          <span className={`status-dot ${healthStatus.users}`}></span>
          Users: {healthStatus.users.toUpperCase()}
        </div>
        <div className="status-badge" title="Routed via Gateway">
          <span className={`status-dot ${healthStatus.linkage}`}></span>
          Linkage: {healthStatus.linkage.toUpperCase()}
        </div>
        <button className="btn-secondary" onClick={checkHealth} title="Actualizar estados">
          🔄
        </button>
        <button className="btn-secondary" onClick={logout} style={{ borderColor: 'var(--accent)', fontWeight: 'bold' }}>
          🚪 Salir
        </button>
      </div>
    </header>
  );
}

export default DashboardHeader;
