import heroBg from '../../assets/hero.png';
import logoUce from '../../assets/images/branding/logo-uce.png';

function AuthLayout({ children }) {
  return (
    <div 
      className="relative min-h-screen flex flex-col justify-center items-center bg-cover bg-center px-4 py-8"
      style={{ backgroundImage: `url(${heroBg})` }}
    >
      {/* Navy blue overlay with 80% opacity */}
      <div className="absolute inset-0 bg-[var(--color-navy-dark)]/80" />

      {/* Content wrapper */}
      <div className="relative z-10 w-full flex flex-col items-center">
        {/* Header section */}
        <header className="flex flex-col items-center mb-8 text-center">
          <img src={logoUce} alt="Logo UCE" className="h-16 w-auto mb-3" />
          <h1 className="text-4xl font-extrabold text-white tracking-wider">SIIU</h1>
          <p className="text-xs text-gray-300 tracking-[0.2em] font-semibold mt-1">ACADÉMICO</p>
        </header>

        {/* Auth card */}
        <main className="bg-[var(--color-navy-card)] rounded-2xl shadow-2xl p-8 w-full max-w-md">
          {children}
        </main>
      </div>
    </div>
  );
}

export default AuthLayout;
