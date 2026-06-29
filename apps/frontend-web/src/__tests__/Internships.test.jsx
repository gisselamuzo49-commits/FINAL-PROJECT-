import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Internships from '../pages/Internships';
import { MemoryRouter } from 'react-router-dom';

// Mock useOutletContext
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useOutletContext: () => ({
      getHeaders: () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer test' }),
      logout: vi.fn(),
    }),
  };
});

const mockFetch = vi.fn();
globalThis.fetch = mockFetch;

const mockPasantias = [
  {
    id: 101,
    title: "Desarrollador React Junior",
    company: "Tech Solutions",
    description: "Desarrollo frontend usando React y Vite.",
    status: "ABIERTA"
  }
];

const mockAplicantes = [
  {
    postulacionId: 501,
    estudianteId: "2",
    nombreEstudiante: "Carlos Pérez",
    mensaje: "Interesado en la oferta",
    estado: "PENDIENTE"
  }
];

describe('Internships.jsx Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('1. verifies "Postularse" button appears for STUDENT', async () => {
    // JWT de Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/internships/applications/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve([]), // Sin postulaciones previas
        });
      }
      if (url.endsWith('/api/internships')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockPasantias),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });

    render(
      <MemoryRouter>
        <Internships />
      </MemoryRouter>
    );

    // Esperar a que se carguen las ofertas
    await waitFor(() => {
      expect(screen.getByText(/Desarrollador React Junior/i)).toBeInTheDocument();
    });

    // Verificar que el botón "Postularse" está visible para el estudiante
    const postularBtn = screen.getByRole('button', { name: /Postularse/i });
    expect(postularBtn).toBeInTheDocument();
    expect(postularBtn).not.toBeDisabled();
  });

  it('2. verifies "Postularse" button does NOT appear for TUTOR', async () => {
    // JWT de Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.endsWith('/api/internships')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockPasantias),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });

    render(
      <MemoryRouter>
        <Internships />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Desarrollador React Junior/i)).toBeInTheDocument();
    });

    // El botón "Postularse" no debe aparecer para el tutor
    const postularBtn = screen.queryByRole('button', { name: /Postularse/i });
    expect(postularBtn).toBeNull();
  });

  it('3. verifies "Postulaciones Recibidas" panel appears for TUTOR upon selecting an internship', async () => {
    // JWT de Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.endsWith('/api/internships')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockPasantias),
        });
      }
      if (url.includes('/api/internships/101/applications')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockAplicantes),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });

    render(
      <MemoryRouter>
        <Internships />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Desarrollador React Junior/i)).toBeInTheDocument();
    });

    // Hacer clic en "Ver Aplicantes" de la oferta 101
    const verAplicantesBtn = screen.getByRole('button', { name: /Ver Aplicantes/i });
    expect(verAplicantesBtn).toBeInTheDocument();
    fireEvent.click(verAplicantesBtn);

    // Verificar que aparece el panel de "Postulaciones Recibidas"
    await waitFor(() => {
      expect(screen.getByText(/Postulaciones Recibidas \(Oferta #101\)/i)).toBeInTheDocument();
    });

    // Verificar que aparece el nombre del estudiante postulante
    expect(screen.getByText(/Carlos Pérez/i)).toBeInTheDocument();
    expect(screen.getByText(/Interesado en la oferta/i)).toBeInTheDocument();
  });

  it('4. mocks API call to postularse successfully and disables button', async () => {
    // JWT de Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    // Primera llamada: sin postulaciones. Segunda llamada (tras postularse): ya con postulación.
    let callCount = 0;
    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/internships/applications/student/1')) {
        callCount++;
        const responseData = callCount > 1 ? [{ internshipId: 101 }] : [];
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(responseData),
        });
      }
      if (url.endsWith('/api/internships')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockPasantias),
        });
      }
      if (url.includes('/api/internships/101/applications') && mockFetch.mock.calls.some(c => c[1]?.method === 'POST')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ id: 999, studentId: "1", internshipId: 101, estado: "PENDIENTE" }),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });

    render(
      <MemoryRouter>
        <Internships />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Desarrollador React Junior/i)).toBeInTheDocument();
    });

    const postularBtn = screen.getByRole('button', { name: /Postularse/i });
    expect(postularBtn).not.toBeDisabled();

    // Simular el clic en Postularse
    fireEvent.click(postularBtn);

    // Verificar que se llame al endpoint POST correcto
    await waitFor(() => {
      const postCalls = mockFetch.mock.calls.filter(c => c[0].includes('/api/internships/101/applications'));
      expect(postCalls.length).toBeGreaterThan(0);
      expect(JSON.parse(postCalls[0][1].body)).toEqual({ estudianteId: "1", mensaje: "Solicitud de postulación" });
    });

    // Verificar que se muestre el mensaje de éxito
    await waitFor(() => {
      expect(screen.getByText(/¡Postulación enviada exitosamente!/i)).toBeInTheDocument();
    });

    // El botón ahora debe estar deshabilitado con el texto "Ya postulado"
    await waitFor(() => {
      const yaPostuladoBtn = screen.getByRole('button', { name: /Ya postulado/i });
      expect(yaPostuladoBtn).toBeInTheDocument();
      expect(yaPostuladoBtn).toBeDisabled();
    });
  });
});
