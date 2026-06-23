import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import CheckEmail from './pages/CheckEmail';
import DashboardLayout from './components/dashboard/DashboardLayout';
import Home from './pages/Home';
import ProtectedRoute from './components/auth/ProtectedRoute';
import Internships from './pages/Internships';
import MyApplications from './pages/MyApplications';
import Linkage from './pages/Linkage';
import Users from './pages/Users';
import Hours from './pages/Hours';
import Evaluations from './pages/Evaluations';
import Documents from './pages/Documents';
import Notifications from './pages/Notifications';
import Recommendations from './pages/Recommendations';
import Reports from './pages/Reports';

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
        path: 'internships/applications',
        element: <MyApplications />
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
      },
      {
        path: 'evaluations',
        element: <Evaluations />
      },
      {
        path: 'documents',
        element: <Documents />
      },
      {
        path: 'notifications',
        element: <Notifications />
      },
      {
        path: 'recommendations',
        element: <Recommendations />
      },
      {
        path: 'reports',
        element: <Reports />
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
