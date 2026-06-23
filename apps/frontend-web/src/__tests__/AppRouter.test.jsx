import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Intercept createBrowserRouter and return createMemoryRouter instead
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    createBrowserRouter: (routes, opts) => {
      globalThis.activeRouter = actual.createMemoryRouter(routes, opts);
      return globalThis.activeRouter;
    },
  };
});

// Mock ProtectedRoute to render children directly
vi.mock('../components/auth/ProtectedRoute', () => ({
  default: ({ children }) => children,
}));

import AppRouter from '../AppRouter';

// Mock fetch globally
const mockFetch = vi.fn();
globalThis.fetch = mockFetch;

describe('AppRouter Routing Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    localStorage.setItem('token', 'fake-jwt-token');
    localStorage.setItem('userEmail', 'estudiante@uce.edu.ec');

    // Default fetch mock implementation returning specific page structures
    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/users/email/')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ id: "1", firstName: "Ana", lastName: "Torres", role: "ESTUDIANTE" }),
        });
      }
      if (url.includes('/api/documents/student/')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ totalDocumentos: 0, documentos: [] }),
        });
      }
      if (url.includes('/api/hours/student/')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ totalHorasValidadas: 0, totalHorasPendientes: 0 }),
        });
      }
      if (url.includes('/api/reports/student/')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ id: 1, totalHorasValidadas: 0, totalDocumentos: 0, ultimaActualizacion: "2026-06-22T12:00:00Z" }),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });
  });

  const navigateTo = async (path) => {
    await globalThis.activeRouter.navigate(path);
  };

  it('renders Login on /login', async () => {
    localStorage.removeItem('token'); // Clear token so Login doesn't redirect
    render(<AppRouter />);
    await navigateTo('/login');
    await waitFor(() => {
      expect(screen.getAllByText(/Sign in/i).length).toBeGreaterThan(0);
    });
  });

  it('redirects to /home and renders Home on /', async () => {
    render(<AppRouter />);
    await navigateTo('/');
    await waitFor(() => {
      expect(screen.getAllByText(/Bienvenido/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Home on /home', async () => {
    render(<AppRouter />);
    await navigateTo('/home');
    await waitFor(() => {
      expect(screen.getAllByText(/Bienvenido/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Internships on /internships', async () => {
    render(<AppRouter />);
    await navigateTo('/internships');
    await waitFor(() => {
      expect(screen.getAllByText(/Pasant/i).length).toBeGreaterThan(0);
    });
  });

  it('renders MyApplications on /internships/applications', async () => {
    render(<AppRouter />);
    await navigateTo('/internships/applications');
    await waitFor(() => {
      expect(screen.getAllByText(/Postula/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Linkage on /linkage', async () => {
    render(<AppRouter />);
    await navigateTo('/linkage');
    await waitFor(() => {
      expect(screen.getAllByText(/Vinculaci/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Users on /users', async () => {
    render(<AppRouter />);
    await navigateTo('/users');
    await waitFor(() => {
      expect(screen.getAllByText(/Usuari/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Hours on /hours', async () => {
    render(<AppRouter />);
    await navigateTo('/hours');
    await waitFor(() => {
      expect(screen.getAllByText(/Hora/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Evaluations on /evaluations', async () => {
    render(<AppRouter />);
    await navigateTo('/evaluations');
    await waitFor(() => {
      expect(screen.getAllByText(/Evaluaci/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Documents on /documents', async () => {
    render(<AppRouter />);
    await navigateTo('/documents');
    await waitFor(() => {
      expect(screen.getAllByText(/Document/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Notifications on /notifications', async () => {
    render(<AppRouter />);
    await navigateTo('/notifications');
    await waitFor(() => {
      expect(screen.getAllByText(/Notificaci/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Recommendations on /recommendations', async () => {
    render(<AppRouter />);
    await navigateTo('/recommendations');
    await waitFor(() => {
      expect(screen.getAllByText(/Recomendaci/i).length).toBeGreaterThan(0);
    });
  });

  it('renders Reports on /reports', async () => {
    render(<AppRouter />);
    await navigateTo('/reports');
    await waitFor(() => {
      expect(screen.getAllByText(/Report/i).length).toBeGreaterThan(0);
    });
  });

  it('redirects to /home on invalid route', async () => {
    render(<AppRouter />);
    await navigateTo('/cualquier-cosa-invalida');
    await waitFor(() => {
      expect(screen.getAllByText(/Bienvenido/i).length).toBeGreaterThan(0);
    });
  });
});
