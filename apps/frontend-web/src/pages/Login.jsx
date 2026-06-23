import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/auth/AuthLayout';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  // Registration form states
  const [showRegister, setShowRegister] = useState(false);
  const [nombre, setNombre] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [rol, setRol] = useState('Estudiante');
  const [showRegPassword, setShowRegPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [regError, setRegError] = useState(null);
  const [regSuccess, setRegSuccess] = useState(null);

  if (localStorage.getItem('token')) {
    return <Navigate to="/home" replace />;
  }

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);
    const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';
    fetch(`${API}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password })
    })
      .then(response => {
        if (!response.ok) throw new Error("Credenciales inválidas");
        return response.json();
      })
      .then(data => {
        localStorage.setItem("token", data.token);
        localStorage.setItem("userEmail", data.email);
        navigate("/home");
      })
      .catch(err => setError(err.message));
  };

  const handleRegister = (e) => {
    e.preventDefault();
    setRegError(null);
    setRegSuccess(null);

    // Validate email ends with @uce.edu.ec
    if (!regEmail.endsWith('@uce.edu.ec')) {
      setRegError('El correo debe pertenecer al dominio institucional (@uce.edu.ec)');
      return;
    }

    // Validate password confirmation
    if (regPassword !== confirmPassword) {
      setRegError('Las contraseñas no coinciden');
      return;
    }

    const API = import.meta.env.VITE_API_BASE_URL || 'http://18.232.199.190:8082';
    fetch(`${API}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nombre, email: regEmail, password: regPassword, rol })
    })
      .then(response => {
        return response.text().then(text => {
          if (!response.ok) {
            throw new Error(text || "Error al registrar el usuario");
          }
          if (text.toLowerCase().includes("error")) {
            throw new Error(text);
          }
          return text;
        });
      })
      .then(message => {
        setRegSuccess(message || '¡Éxito! Usuario registrado en la base de datos.');
        // Auto-fill login inputs and switch to login view
        setTimeout(() => {
          setEmail(regEmail);
          setPassword(regPassword);
          setShowRegister(false);
          setRegSuccess(null);
          // Clean up registration fields
          setNombre('');
          setRegEmail('');
          setRegPassword('');
          setConfirmPassword('');
          setRol('Estudiante');
        }, 2000);
      })
      .catch(err => setRegError(err.message));
  };

  if (showRegister) {
    return (
      <AuthLayout>
        <div className="flex flex-col items-center">
          <h2 className="text-2xl font-bold text-white mb-6 text-center">Registro</h2>
          
          <form onSubmit={handleRegister} className="w-full space-y-5">
            {/* Nombre completo input */}
            <div className="flex flex-col space-y-2">
              <label className="text-sm font-medium text-gray-200 text-left">Nombre completo</label>
              <input
                type="text"
                required
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="Juan Pérez"
              />
            </div>

            {/* Email input */}
            <div className="flex flex-col space-y-2">
              <label className="text-sm font-medium text-gray-200 text-left">Email Address</label>
              <input
                type="email"
                required
                value={regEmail}
                onChange={(e) => setRegEmail(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="email@uce.edu.ec"
              />
            </div>

            {/* Password input */}
            <div className="flex flex-col space-y-2 relative">
              <label className="text-sm font-medium text-gray-200 text-left">Password</label>
              <div className="relative">
                <input
                  type={showRegPassword ? 'text' : 'password'}
                  required
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 pr-12 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowRegPassword(!showRegPassword)}
                  className="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-600 hover:text-gray-900 focus:outline-none"
                >
                  {showRegPassword ? '🙈' : '👁'}
                </button>
              </div>
            </div>

            {/* Confirmar password input */}
            <div className="flex flex-col space-y-2 relative">
              <label className="text-sm font-medium text-gray-200 text-left">Confirmar password</label>
              <div className="relative">
                <input
                  type={showConfirmPassword ? 'text' : 'password'}
                  required
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 pr-12 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-600 hover:text-gray-900 focus:outline-none"
                >
                  {showConfirmPassword ? '🙈' : '👁'}
                </button>
              </div>
            </div>

            {/* Rol input selector */}
            <div className="flex flex-col space-y-2">
              <label className="text-sm font-medium text-gray-200 text-left">Rol</label>
              <select
                value={rol}
                onChange={(e) => setRol(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] appearance-none cursor-pointer"
              >
                <option value="Estudiante">Estudiante</option>
                <option value="Tutor">Tutor</option>
                <option value="Coordinador">Coordinador</option>
              </select>
            </div>

            {/* Submit button */}
            <button
              type="submit"
              className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white rounded-lg py-3 w-full font-semibold transition shadow-md focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] focus:ring-offset-2 focus:ring-offset-[var(--color-navy-card)]"
            >
              Crear cuenta
            </button>
          </form>

          {regError && <p className="text-red-400 text-sm text-center mt-4">{regError}</p>}
          {regSuccess && <p className="text-green-400 text-sm text-center mt-4 font-semibold">{regSuccess}</p>}

          <p className="text-sm text-gray-300 mt-4 text-center">
            ¿Ya tienes cuenta?{' '}
            <button 
              onClick={() => {
                setShowRegister(false);
                setRegError(null);
                setRegSuccess(null);
              }}
              type="button"
              className="text-[var(--color-gold)] hover:underline focus:outline-none cursor-pointer"
            >
              Inicia sesión aquí
            </button>
          </p>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className="flex flex-col items-center">
        <h2 className="text-2xl font-bold text-white mb-6 text-center">Sign in</h2>
        
        <form onSubmit={handleSubmit} className="w-full space-y-5">
          {/* Email input */}
          <div className="flex flex-col space-y-2">
            <label className="text-sm font-medium text-gray-200 text-left">Email Address</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
              placeholder="email@uce.edu.ec"
            />
          </div>

          {/* Password input */}
          <div className="flex flex-col space-y-2 relative">
            <label className="text-sm font-medium text-gray-200 text-left">Password</label>
            <div className="relative">
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="bg-gray-100 rounded-lg px-4 py-3 w-full text-gray-900 pr-12 focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
                placeholder="••••••••"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-600 hover:text-gray-900 focus:outline-none"
              >
                {showPassword ? '🙈' : '👁'}
              </button>
            </div>
          </div>

          {/* Remember me & Forgot password */}
          <div className="flex items-center justify-between text-sm text-gray-300">
            <label className="flex items-center space-x-2 cursor-pointer select-none">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="rounded border-gray-400 text-[var(--color-purple)] focus:ring-[var(--color-purple)] bg-gray-100 h-4 w-4"
              />
              <span>Recordarme</span>
            </label>
            
            <Link 
              to="/forgot-password" 
              className="hover:text-white transition duration-200"
            >
              Forgot password?
            </Link>
          </div>

          {/* Submit button */}
          <button
            type="submit"
            className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white rounded-lg py-3 w-full font-semibold transition shadow-md focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)] focus:ring-offset-2 focus:ring-offset-[var(--color-navy-card)]"
          >
            Sign in
          </button>
        </form>

        {error && <p className="text-red-400 text-sm text-center mt-4">{error}</p>}

        <p className="text-sm text-gray-300 mt-4 text-center">
          ¿No tienes cuenta?{' '}
          <button 
            onClick={() => {
              setShowRegister(true);
              setError(null);
            }} 
            type="button"
            className="text-[var(--color-gold)] hover:underline focus:outline-none cursor-pointer"
          >
            Regístrate aquí
          </button>
        </p>
      </div>
    </AuthLayout>
  );
}

export default Login;
