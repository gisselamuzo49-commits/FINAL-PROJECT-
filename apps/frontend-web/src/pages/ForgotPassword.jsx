import { useState } from 'react';
import { Link } from 'react-router-dom';
import AuthLayout from '../components/auth/AuthLayout';

function ForgotPassword() {
  const [email, setEmail] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    // TODO: integrar con endpoint de recuperación
    console.log('Forgot Password Submit:', email);
  };

  return (
    <AuthLayout>
      <div className="flex flex-col items-center">
        {/* Lock Icon */}
        <div className="text-4xl mb-4">🔒</div>
        
        <h2 className="text-2xl font-bold text-white mb-2 text-center">¿Olvidaste tu contraseña?</h2>
        <p className="text-sm text-gray-300 text-center mb-6">
          Ingresa tu email para reiniciar tu contraseña.
        </p>

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

          {/* Submit button */}
          <button
            type="submit"
            className="bg-[var(--color-purple)] hover:bg-[var(--color-purple-hover)] text-white rounded-lg py-3 w-full font-semibold transition shadow-md focus:outline-none focus:ring-2 focus:ring-[var(--color-purple)]"
          >
            Send Email
          </button>
        </form>

        <div className="mt-6 text-center">
          <Link 
            to="/login" 
            className="text-sm text-gray-300 hover:text-white transition duration-200"
          >
            &lt; Back to Login
          </Link>
        </div>
      </div>
    </AuthLayout>
  );
}

export default ForgotPassword;
