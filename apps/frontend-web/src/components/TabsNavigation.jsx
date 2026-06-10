function TabsNavigation({ activeTab, setActiveTab }) {
  return (
    <nav className="tabs-navigation" role="tablist">
      <button
        className={`tab-btn ${activeTab === 'internships' ? 'active' : ''}`}
        onClick={() => setActiveTab('internships')}
        role="tab"
        aria-selected={activeTab === 'internships'}
      >
        💼 Ofertas Pasantías
      </button>
      <button
        className={`tab-btn ${activeTab === 'profiles' ? 'active' : ''}`}
        onClick={() => setActiveTab('profiles')}
        role="tab"
        aria-selected={activeTab === 'profiles'}
      >
        👤 Perfiles Estudiantes
      </button>
      <button
        className={`tab-btn ${activeTab === 'linkage' ? 'active' : ''}`}
        onClick={() => setActiveTab('linkage')}
        role="tab"
        aria-selected={activeTab === 'linkage'}
      >
        🔗 Proyectos Vinculación
      </button>
    </nav>
  );
}

export default TabsNavigation;
