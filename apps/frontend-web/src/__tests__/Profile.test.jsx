import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import Profile from '../pages/Profile';
import { MemoryRouter } from 'react-router-dom';

// Mock useOutletContext
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useOutletContext: () => ({
      getHeaders: () => ({ 'Content-Type': 'application/json', 'Authorization': 'Bearer test-token' }),
      logout: vi.fn(),
    }),
  };
});

const mockFetch = vi.fn();
globalThis.fetch = mockFetch;

describe('Profile.jsx E2E User Profile CRUD', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    // Student JWT token payload: { nombre: "Gissela Muzo", rol: "STUDENT", id: "1", sub: "gisse@uce.edu.ec" }
    localStorage.setItem('token', 'header.eyJub21icmUiOiJHaXNzZWxhIE11em8iLCJyb2wiOiJTVFVERU5UIiwiaWQiOiIxIiwic3ViIjoiZ2lzc2VAdWNlLmVkdS5lYyJ9.signature');

    mockFetch.mockImplementation((url) => {
      if (url.includes('/api/users/profile/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({
            id: 1,
            firstName: 'Gissela',
            lastName: 'Muzo',
            email: 'gisse@uce.edu.ec',
            phone: '0999999999',
            role: 'STUDENT',
            carrera: 'Sistemas',
            facultad: 'Ingeniería',
            habilidades: 'React, Spring Boot',
            cursos: 'Scrum',
            experiencia: 'Ninguna',
            descripcion: 'Estudiante UCE'
          }),
        });
      }
      if (url.includes('/api/internships/applications/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve([{ id: 'a1', estado: 'ACEPTADA' }]),
        });
      }
      if (url.includes('/api/hours/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve({ totalHorasValidadas: 150 }),
        });
      }
      if (url.includes('/api/evaluations/student/1')) {
        return Promise.resolve({
          ok: true,
          status: 200,
          json: () => Promise.resolve([{ id: 'e1', score: 9.5 }]),
        });
      }
      return Promise.resolve({
        ok: true,
        status: 200,
        json: () => Promise.resolve({}),
      });
    });
  });

  it('verifies that the profile form renders with data', async () => {
    render(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );

    // Verify fields are rendered
    await waitFor(() => {
      expect(screen.getByText(/Mi Perfil de Usuario/i)).toBeInTheDocument();
      expect(screen.getByText(/gisse@uce.edu.ec/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/Gissela/i)).toBeInTheDocument();
    expect(screen.getByText(/Muzo/i)).toBeInTheDocument();
    expect(screen.getByText(/Sistemas/i)).toBeInTheDocument();
    expect(screen.getByText(/React, Spring Boot/i)).toBeInTheDocument();
  });

  it('verifies that the Edit and Save buttons exist and work', async () => {
    render(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getAllByText(/Editar/i).length).toBeGreaterThan(0);
    });

    const editBtn = screen.getAllByText(/Editar/i)[0];
    editBtn.click();

    // After clicking edit, Save and Cancel buttons should exist
    await waitFor(() => {
      expect(screen.getByText(/Guardar/i)).toBeInTheDocument();
      expect(screen.getByText(/Cancelar/i)).toBeInTheDocument();
    });
  });

  it('verifies that the Delete Account button exists', async () => {
    render(
      <MemoryRouter>
        <Profile />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Eliminar Cuenta/i)).toBeInTheDocument();
    });
  });
});
