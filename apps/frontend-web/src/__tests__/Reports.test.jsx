import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Reports from '../pages/Reports';
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

describe('Reports.jsx View', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    localStorage.setItem('token', 'mocked-jwt-token');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/reports/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({
            id: "R001",
            totalHorasValidadas: 120,
            totalDocumentos: 2,
            ultimaActualizacion: "2026-06-22T12:00:00Z"
          }),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve([]),
      });
    });
  });

  it('renders student reports and simulates report generation banner', async () => {
    render(
      <MemoryRouter>
        <Reports />
      </MemoryRouter>
    );

    // Verify R001 exists in the table
    await waitFor(() => {
      expect(screen.getByText(/R001/i)).toBeInTheDocument();
    });

    // Verify "Generar Reporte" button exists
    const generateBtn = screen.getByRole('button', { name: /Generar Reporte/i });
    expect(generateBtn).toBeInTheDocument();

    // Click "Generar Reporte"
    fireEvent.click(generateBtn);

    // Verify banner message appears
    expect(screen.getByText(/Generación de reportes en desarrollo/i)).toBeInTheDocument();
  });
});
