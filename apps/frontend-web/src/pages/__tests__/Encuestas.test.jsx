import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Encuestas from '../Encuestas';
import { MemoryRouter } from 'react-router-dom';

// Setup Mock for useOutletContext
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useOutletContext: () => ({
      getHeaders: () => ({ 'Content-Type': 'application/json' }),
      logout: vi.fn(),
    }),
  };
});

// Setup dynamic mocks for Supabase client chaining
const mockOrder = vi.fn();
const mockEq = vi.fn(() => ({ order: mockOrder }));
const mockSelect = vi.fn(() => ({ eq: mockEq }));
const mockInsert = vi.fn();

vi.mock('../../lib/supabaseClient', () => {
  return {
    supabase: {
      from: vi.fn(() => ({
        select: mockSelect,
        insert: mockInsert
      }))
    }
  };
});

describe('Encuestas.jsx Component Unit Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    // Inyectar JWT mockeado
    localStorage.setItem('token', 'header.eyJub21icmUiOiJHaXNzZWxhIE11em8iLCJyb2wiOiJFU1RVRElBTlRFIiwiaWQiOiIxIiwic3ViIjoiZ2lzc2VsYUB1Y2UuZWR1LmVjIn0.signature');
  });

  it('renders the survey form successfully', async () => {
    mockOrder.mockResolvedValue({ data: [], error: null });

    render(
      <MemoryRouter>
        <Encuestas />
      </MemoryRouter>
    );

    // Verificar que el título principal de la vista renderiza
    expect(screen.getByText(/Encuestas de Satisfacción Post-Práctica/i)).toBeInTheDocument();

    // Verificar que los campos requeridos existen en el formulario
    expect(screen.getByLabelText(/Empresa \/ Institución/i)).toBeInTheDocument();
    expect(screen.getByText(/Calificación/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Comentarios o Sugerencias/i)).toBeInTheDocument();

    // Verificar que el botón de envío existe
    const submitBtn = screen.getByRole('button', { name: /Enviar Encuesta/i });
    expect(submitBtn).toBeInTheDocument();
  });

  it('displays the list of previously submitted surveys (mocked from Supabase)', async () => {
    mockOrder.mockResolvedValue({
      data: [
        {
          id: 101,
          empresa: 'Banco Pichincha',
          calificacion: 4,
          comentario: 'Excelente mentoría y aprendizaje continuo.',
          nombre_estudiante: 'Gissela Muzo'
        }
      ],
      error: null
    });

    render(
      <MemoryRouter>
        <Encuestas />
      </MemoryRouter>
    );

    // Esperar a que se cargue y se muestre la encuesta mockeada
    await waitFor(() => {
      expect(screen.getByText('Banco Pichincha')).toBeInTheDocument();
      expect(screen.getByText('Excelente mentoría y aprendizaje continuo.')).toBeInTheDocument();
    });
  });

  it('handles and displays errors when Supabase fails to load existing surveys', async () => {
    mockOrder.mockResolvedValue({
      data: null,
      error: { message: 'Database connection failed' }
    });

    render(
      <MemoryRouter>
        <Encuestas />
      </MemoryRouter>
    );

    // Esperar a que se muestre el mensaje de error en pantalla
    await waitFor(() => {
      expect(screen.getByText(/No se pudieron cargar las encuestas anteriores./i)).toBeInTheDocument();
    });
  });

  it('submits a new survey and refreshes the list', async () => {
    mockOrder.mockResolvedValue({ data: [], error: null });
    mockInsert.mockResolvedValue({ error: null });

    render(
      <MemoryRouter>
        <Encuestas />
      </MemoryRouter>
    );

    // Completar el formulario
    const inputEmpresa = screen.getByLabelText(/Empresa \/ Institución/i);
    fireEvent.change(inputEmpresa, { target: { value: 'Telconet' } });

    const txtComentario = screen.getByLabelText(/Comentarios o Sugerencias/i);
    fireEvent.change(txtComentario, { target: { value: 'Prácticas muy enriquecedoras' } });

    // Enviar formulario
    const submitBtn = screen.getByRole('button', { name: /Enviar Encuesta/i });
    fireEvent.click(submitBtn);

    // Verificar que el mensaje de éxito aparece
    await waitFor(() => {
      expect(screen.getByText(/¡Encuesta enviada con éxito!/i)).toBeInTheDocument();
    });

    // Validar que Supabase insert haya sido llamado con los parámetros correctos
    expect(mockInsert).toHaveBeenCalledWith([
      {
        estudiante_id: '1',
        nombre_estudiante: 'Gissela Muzo',
        empresa: 'Telconet',
        calificacion: 5,
        comentario: 'Prácticas muy enriquecedoras'
      }
    ]);
  });
});
