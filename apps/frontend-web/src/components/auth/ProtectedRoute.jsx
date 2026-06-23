import { Navigate } from 'react-router-dom';

function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;

  let isExpired = false;
  let hasError = false;

  try {
    const parts = token.split('.');
    if (parts.length === 3) {
      const payload = JSON.parse(atob(parts[1]));
      // eslint-disable-next-line react-hooks/purity
      isExpired = (payload.exp * 1000) < Date.now();
    } else {
      hasError = true;
    }
  } catch {
    hasError = true;
  }

  if (isExpired || hasError) {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;

