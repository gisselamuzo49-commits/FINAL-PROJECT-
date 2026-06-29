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
      getHeaders: () => ({ 'Content-Type': 'application/json' }),
      logout: vi.fn(),
    }),
  };
});

const mockFetch = vi.fn();
globalThis.fetch = mockFetch;

const mockStudentReport = {
  id: "R001",
  totalHorasValidadas: 120,
  totalDocumentos: 2,
  ultimaActualizacion: "2026-06-22T12:00:00Z"
};

const mockGlobalReport = {
  id: "global",
  totalEstudiantes: 42,
  totalHorasValidadas: 3200,
  totalHorasPendientes: 500,
  estudiantesPorFacultad: { "Ingeniería": 20, "Ciencias": 22 },
  ultimaActualizacion: "2026-06-22T12:00:00Z"
};

const mockMetrics = {
  database: "InfluxDB",
  bucket: "horas-metricas",
  status: "connected"
};

describe('Reports.jsx View', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('renders student reports and simulates report generation', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/reports/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentReport),
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

    // Verify success banner message appears
    await waitFor(() => {
      expect(screen.getByText(/Reporte generado exitosamente/i)).toBeInTheDocument();
    });
  });

  it('calls the correct endpoint when "Generar Reporte" is clicked by STUDENT', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/reports/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentReport),
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
        <Reports />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/R001/i)).toBeInTheDocument();
    });

    // Clear mocks to track only the generate call
    mockFetch.mockClear();
    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/reports/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentReport),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve({}),
      });
    });

    const generateBtn = screen.getByRole('button', { name: /Generar Reporte/i });
    fireEvent.click(generateBtn);

    // Verify that the student endpoint was called
    await waitFor(() => {
      const studentCalls = mockFetch.mock.calls.filter(c => c[0].includes('/api/reports/student/1'));
      expect(studentCalls.length).toBeGreaterThan(0);
    });

    // Verify that the success banner appears
    await waitFor(() => {
      expect(screen.getByText(/Reporte generado exitosamente/i)).toBeInTheDocument();
    });
  });

  it('shows InfluxDB metrics card for TUTOR', async () => {
    // JWT Tutor
    localStorage.setItem('token', 'header.eyJub21icmUiOiJKdWFuIFBvd2VycyIsInJvbCI6IlRVVE9SIiwiaWQiOiIyIiwic3ViIjoidHV0b3JAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/reports/student/2')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentReport),
        });
      }
      if (url.includes('/api/reports/global')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockGlobalReport),
        });
      }
      if (url.includes('/api/reports/metrics/stats')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockMetrics),
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
        <Reports />
      </MemoryRouter>
    );

    // Verify InfluxDB section appears for tutor
    await waitFor(() => {
      expect(screen.getByText(/Métricas de Series Temporales/i)).toBeInTheDocument();
    });

    // Verify InfluxDB badge (appears in heading and badge, so use getAllByText)
    const influxElements = screen.getAllByText(/INFLUXDB/i);
    expect(influxElements.length).toBeGreaterThanOrEqual(1);

    // Verify metrics data is loaded
    await waitFor(() => {
      expect(screen.getByText(/horas-metricas/i)).toBeInTheDocument();
      expect(screen.getByText(/connected/i)).toBeInTheDocument();
    });
  });

  it('does NOT show InfluxDB metrics card for STUDENT', async () => {
    // JWT Estudiante
    localStorage.setItem('token', 'header.eyJub21icmUiOiJBbmEgVG9ycmVzIiwicm9sIjoiRVNUVURJQU5URSIsImlkIjoiMSIsInN1YiI6InRlc3RAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/reports/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve(mockStudentReport),
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
        <Reports />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/R001/i)).toBeInTheDocument();
    });

    // InfluxDB section should NOT be present
    expect(screen.queryByText(/Métricas de Series Temporales/i)).toBeNull();
    expect(screen.queryByText(/INFLUXDB/i)).toBeNull();
  });
});
