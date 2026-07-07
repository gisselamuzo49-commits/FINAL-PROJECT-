import { Navigate } from 'react-router-dom';

const RoleProtectedRoute = ({ children, allowedRoles }) => {
  let isAuthorized = false;
  let hasToken = false;

  try {
    const token = localStorage.getItem('token');
    if (token) {
      hasToken = true;
      const payload = JSON.parse(atob(token.split('.')[1]));
      const userRole = (payload.rol || payload.role || '').toUpperCase();
      
      if (!allowedRoles || allowedRoles.includes(userRole)) {
        isAuthorized = true;
      }
    }
  } catch {
    isAuthorized = false;
  }

  if (!hasToken) {
    return <Navigate to="/login" replace />;
  }

  if (!isAuthorized) {
    return <Navigate to="/home" replace />;
  }

  return children;
};

export default RoleProtectedRoute;
