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

  if (localStorage.getItem('token')) {
    return <Navigate to="/home" replace />;
  }

  const handleSubmit = (e) => {
    e.preventDefault();
    setError(null);
    const API_URL = `http://${window.location.hostname}`;
    const GATEWAY_PORT = import.meta.env.VITE_GATEWAY_PORT || "8082";
    fetch(`${API_URL}:${GATEWAY_PORT}/api/auth/login`, {
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
      </div>
    </AuthLayout>
  );
}

export default Login;
