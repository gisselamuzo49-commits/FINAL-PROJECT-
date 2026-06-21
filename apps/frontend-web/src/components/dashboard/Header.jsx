function Header({ onMenuClick, notificationCount = 0 }) {
  return (
    <header className="bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center shadow-sm">
      {/* Left side: Hamburger button for mobile only */}
      <div className="flex items-center">
        <button
          onClick={onMenuClick}
          className="lg:hidden text-gray-600 hover:text-gray-900 focus:outline-none p-1.5 rounded-lg hover:bg-gray-100 transition-colors"
          aria-label="Abrir menú"
        >
          <span className="text-2xl">☰</span>
        </button>
      </div>

      {/* Right side: Language and Notifications */}
      <div className="flex items-center space-x-6">
        {/* Language icon (placeholder) */}
        <button 
          className="text-gray-500 hover:text-gray-900 transition-colors focus:outline-none"
          title="Cambiar idioma"
        >
          <span className="text-xl">🌐</span>
        </button>

        {/* Notifications with red badge */}
        <button 
          className="relative text-gray-500 hover:text-gray-900 transition-colors focus:outline-none p-1"
          title="Notificaciones"
        >
          <span className="text-xl">🔔</span>
          {notificationCount > 0 && (
            <span className="absolute top-0 right-0 transform translate-x-1 -translate-y-1 bg-red-500 text-white text-[10px] font-bold rounded-full h-5 w-5 flex items-center justify-center border-2 border-white animate-pulse">
              {notificationCount}
            </span>
          )}
        </button>
      </div>
    </header>
  );
}

export default Header;
