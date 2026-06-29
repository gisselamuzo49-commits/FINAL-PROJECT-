import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Evaluations from '../pages/Evaluations';
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

const mockEvaluaciones = [
  {
    id: 801,
    proyectoId: "PROY-01",
    fechaEvaluacion: "2026-06-25",
    nombre: "Tutor Académico 1",
    calificacion: 9.2,
    comentarios: "Excelente desempeño técnico y puntualidad."
  }
];

describe('Evaluations.jsx Component Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('1. verifies evaluation list appears for STUDENT', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/evaluations/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEvaluaciones),
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
        <Evaluations />
      </MemoryRouter>
    );

    // Esperar a que se cargue la lista de evaluaciones
    await waitFor(() => {
      expect(screen.getByText(/Mis Evaluaciones/i)).toBeInTheDocument();
    });

    // Validar comentarios y calificacion
    expect(screen.getByText(/Excelente desempeño técnico y puntualidad/i)).toBeInTheDocument();
    expect(screen.getByText(/9.2/i)).toBeInTheDocument();
    expect(screen.getByText(/Nota \/ 10/i)).toBeInTheDocument();
  });

  it('2. verifies "Registrar Evaluación" form appears for TUTOR', async () => {
    // JWT Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    render(
      <MemoryRouter>
        <Evaluations />
      </MemoryRouter>
    );

    // Validar que se muestre el formulario "Registrar Evaluación Académica"
    expect(screen.getByRole('heading', { name: /Registrar Evaluación Académica/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ej. estudiante_01/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ej. PROY-01/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Ej. 9.5/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Registrar Evaluación/i })).toBeInTheDocument();
  });

  it('3. verifies "Registrar Evaluación" form does NOT appear for STUDENT', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/evaluations/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockEvaluaciones),
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
        <Evaluations />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Mis Evaluaciones/i)).toBeInTheDocument();
    });

    // El formulario de tutor no debe estar presente para el estudiante
    expect(screen.queryByRole('heading', { name: /Registrar Evaluación Académica/i })).toBeNull();
    expect(screen.queryByPlaceholderText(/Ej. estudiante_01/i)).toBeNull();
  });

  it('4. verifies score validation prevents submission if out of 0-10 range', async () => {
    // JWT Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    render(
      <MemoryRouter>
        <Evaluations />
      </MemoryRouter>
    );

    // Llenar datos con nota inválida (ej. 11.5)
    fireEvent.change(screen.getByPlaceholderText(/Ej. estudiante_01/i), { target: { value: 'estudiante_01' } });
    fireEvent.change(screen.getByPlaceholderText(/Ej. PROY-01/i), { target: { value: 'PROY-01' } });
    fireEvent.change(screen.getByPlaceholderText(/Ej. 9.5/i), { target: { value: '11.5' } });
    fireEvent.change(screen.getByPlaceholderText(/Ingresa la retroalimentación/i), { target: { value: 'Buen trabajo.' } });

    // Enviar formulario
    const submitBtn = screen.getByRole('button', { name: /Registrar Evaluación/i });
    fireEvent.submit(submitBtn.closest('form'));

    // Debe mostrar un error de validación
    await waitFor(() => {
      expect(screen.getByText(/La calificación debe estar entre 0 y 10./i)).toBeInTheDocument();
    });

    // El fetch de guardado no debe haberse llamado
    expect(mockFetch).not.toHaveBeenCalled();
  });

  it('5. mocks evaluation creation API successfully and resets form', async () => {
    // JWT Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/evaluations')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ id: 888 }),
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
        <Evaluations />
      </MemoryRouter>
    );

    const studentInput = screen.getByPlaceholderText(/Ej. estudiante_01/i);
    const projectInput = screen.getByPlaceholderText(/Ej. PROY-01/i);
    const scoreInput = screen.getByPlaceholderText(/Ej. 9.5/i);
    const commentInput = screen.getByPlaceholderText(/Ingresa la retroalimentación/i);

    fireEvent.change(studentInput, { target: { value: 'estudiante_01' } });
    fireEvent.change(projectInput, { target: { value: 'PROY-01' } });
    fireEvent.change(scoreInput, { target: { value: '9.5' } });
    fireEvent.change(commentInput, { target: { value: 'Buen trabajo.' } });

    // Enviar
    const submitBtn = screen.getByRole('button', { name: /Registrar Evaluación/i });
    fireEvent.submit(submitBtn.closest('form'));

    // Verificar llamada API
    await waitFor(() => {
      const postCalls = mockFetch.mock.calls.filter(c => c[0].includes('/api/evaluations'));
      expect(postCalls.length).toBeGreaterThan(0);
      expect(JSON.parse(postCalls[0][1].body)).toEqual({
        estudianteId: "estudiante_01",
        proyectoId: "PROY-01",
        tutorId: "2",
        calificacion: 9.5,
        comentarios: "Buen trabajo."
      });
    });

    // Verificar éxito y reseteo
    await waitFor(() => {
      expect(screen.getByText(/¡Evaluación registrada exitosamente!/i)).toBeInTheDocument();
      expect(studentInput.value).toBe("");
      expect(projectInput.value).toBe("");
      expect(scoreInput.value).toBe("");
      expect(commentInput.value).toBe("");
    });
  });
});
