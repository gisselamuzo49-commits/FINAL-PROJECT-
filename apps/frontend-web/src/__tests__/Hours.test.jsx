import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Hours from '../pages/Hours';
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

const mockStudentSummary = {
  totalHorasValidadas: 45.0,
  totalHorasPendientes: 12.0,
  historial: [
    {
      registroId: 1001,
      proyectoId: "PROY-01",
      fecha: "2026-06-25",
      horas: 4.0,
      descripcionActividad: "Desarrollo frontend del dashboard",
      estado: "PENDIENTE"
    },
    {
      registroId: 1002,
      proyectoId: "PROY-01",
      fecha: "2026-06-24",
      horas: 6.0,
      descripcionActividad: "Configuración inicial de Vite",
      estado: "VALIDADO"
    }
  ]
};

describe('Hours.jsx Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('1. verifies hours registration form appears for STUDENT', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/hours/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentSummary),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve({}),
      });
    });

    render(
      <MemoryRouter>
        <Hours />
      </MemoryRouter>
    );

    // Esperar a que se cargue la información
    await waitFor(() => {
      expect(screen.getByText(/Horas Validadas/i)).toBeInTheDocument();
    });

    // Validar que se muestre el formulario "Registrar Horas"
    expect(screen.getByRole('heading', { name: /Registrar Horas/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/ID del Proyecto o Convenio de Vinculación/i)).toBeInTheDocument();

    // Validar que se muestre el historial de actividades
    expect(screen.getByText(/Desarrollo frontend del dashboard/i)).toBeInTheDocument();
  });

  it('2. verifies "Validar Horas" search section appears for TUTOR', async () => {
    // JWT Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    render(
      <MemoryRouter>
        <Hours />
      </MemoryRouter>
    );

    // Validar que se muestre el buscador de estudiantes
    expect(screen.getByRole('heading', { name: /Validar Horas de Estudiantes/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ingresa el ID o código de estudiante/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Buscar/i })).toBeInTheDocument();
  });

  it('3. verifies "Validar Horas" search section does NOT appear for STUDENT', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/hours/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentSummary),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve({}),
      });
    });

    render(
      <MemoryRouter>
        <Hours />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Horas Validadas/i)).toBeInTheDocument();
    });

    // El buscador de estudiantes no debe estar presente para el estudiante
    expect(screen.queryByRole('heading', { name: /Validar Horas de Estudiantes/i })).toBeNull();
    expect(screen.queryByPlaceholderText(/Ingresa el ID o código de estudiante/i)).toBeNull();
  });

  it('4. mocks search and validation actions by the TUTOR successfully', async () => {
    // JWT Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    // Mockear la respuesta de búsqueda de estudiante
    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/hours/student/student_test')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentSummary),
        });
      }
      if (url.includes('/api/hours/1001/validar')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ ...mockStudentSummary.historial[0], estado: "VALIDADO" }),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve({}),
      });
    });

    render(
      <MemoryRouter>
        <Hours />
      </MemoryRouter>
    );

    // Ingresar el ID de estudiante a buscar
    const searchInput = screen.getByPlaceholderText(/Ingresa el ID o código de estudiante/i);
    fireEvent.change(searchInput, { target: { value: 'student_test' } });

    // Clic en buscar
    const buscarBtn = screen.getByRole('button', { name: /Buscar/i });
    fireEvent.click(buscarBtn);

    // Esperar a que cargue el dashboard del estudiante
    await waitFor(() => {
      expect(screen.getByText(/Total Acumulado/i)).toBeInTheDocument();
    });

    // Verificar que se listó el registro pendiente
    expect(screen.getByText(/Desarrollo frontend del dashboard/i)).toBeInTheDocument();

    // Obtener y hacer clic en el botón "Aprobar"
    const aprobarBtn = screen.getByRole('button', { name: /Aprobar/i });
    fireEvent.click(aprobarBtn);

    // Verificar que se llame al endpoint PATCH de validación
    await waitFor(() => {
      const patchCalls = mockFetch.mock.calls.filter(c => c[0].includes('/api/hours/1001/validar'));
      expect(JSON.parse(patchCalls[0][1].body)).toEqual({ tutorId: "2", aprobado: true });
    });
  });
});
