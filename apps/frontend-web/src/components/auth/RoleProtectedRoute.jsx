import { Navigate } from 'react-router-dom';
import { getToken, decodeToken, getUserRole } from '../../lib/auth';

const RoleProtectedRoute = ({ children, allowedRoles }) => {
  const token = getToken();

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  const userRole = getUserRole(decodeToken(token));
  const isAuthorized = !allowedRoles || allowedRoles.includes(userRole);

  if (!isAuthorized) {
    return <Navigate to="/home" replace />;
  }

  return children;
};

export default RoleProtectedRoute;
