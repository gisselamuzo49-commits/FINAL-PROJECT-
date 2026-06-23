import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Home from '../pages/Home';
import { MemoryRouter } from 'react-router-dom';

// Mock useOutletContext
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useOutletContext: () => ({
      userProfile: { id: "1", firstName: "Ana", lastName: "Torres", role: "ESTUDIANTE" },
      estudianteId: "1",
      getHeaders: () => ({ 'Content-Type': 'application/json' }),
      logout: vi.fn(),
    }),
  };
});

const mockFetch = vi.fn();
globalThis.fetch = mockFetch;

describe('Home.jsx Dashboard Panels', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/hours/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ totalHorasValidadas: 120, horasCompletadas: 120, totalHorasPendientes: 40 }),
        });
      }
      if (url.includes('/api/internships/applications/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve([{ estado: 'ACEPTADA', company: 'TechCorp', title: 'TechCorp Internship', fechaPostulacion: '2026-06-01T12:00:00Z' }]),
        });
      }
      if (url.includes('/api/notifications/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve([{ id: 'n1', mensaje: 'Test notif', createdAt: '2026-06-22T12:00:00Z', leida: false, tipo: 'HORAS_APROBADAS' }]),
        });
      }
      if (url.includes('/api/internships')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve([{ id: '101', title: 'Dev Junior', description: 'React Developer Position', company: 'TechCorp' }]),
        });
      }
      if (url.includes('/api/ai/recommend')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ recomendaciones: [{ id: '101', score: 95 }] }),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });
  });

  it('renders all panels correctly with simulated student context', async () => {
    render(
      <MemoryRouter>
        <Home />
      </MemoryRouter>
    );

    // Verify Welcome text
    await waitFor(() => {
      expect(screen.getByText(/Bienvenido\/a, Ana Torres/i)).toBeInTheDocument();
    });

    // Verify Donut Chart SVG element exists
    const svgElement = document.querySelector('svg');
    expect(svgElement).toBeInTheDocument();

    // Verify Active Internship Company exists
    await waitFor(() => {
      expect(screen.getAllByText(/TechCorp/i).length).toBeGreaterThan(0);
    });

    // Verify Notification exists
    await waitFor(() => {
      expect(screen.getByText(/Test notif/i)).toBeInTheDocument();
    });
  });
});
