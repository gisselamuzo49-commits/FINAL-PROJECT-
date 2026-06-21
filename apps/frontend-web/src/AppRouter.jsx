import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import CheckEmail from './pages/CheckEmail';
import DashboardLayout from './components/dashboard/DashboardLayout';
import Home from './pages/Home';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Internships from './pages/Internships';
import Linkage from './pages/Linkage';
import Users from './pages/Users';
import Hours from './pages/Hours';

const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/home" replace />
  },
  {
    path: '/login',
    element: <Login />
  },
  {
    path: '/forgot-password',
    element: <ForgotPassword />
  },
  {
    path: '/check-email',
    element: <CheckEmail />
  },
  {
    path: '/',
    element: <ProtectedRoute><DashboardLayout /></ProtectedRoute>,
    children: [
      {
        path: 'home',
        element: <Home />
      },
      {
        path: 'internships',
        element: <Internships />
      },
      {
        path: 'linkage',
        element: <Linkage />
      },
      {
        path: 'users',
        element: <Users />
      },
      {
        path: 'hours',
        element: <Hours />
      }
    ]
  },
  {
    path: '*',
    element: <Navigate to="/" replace />
  }
]);

function AppRouter() {
  return <RouterProvider router={router} />;
}

export default AppRouter;
