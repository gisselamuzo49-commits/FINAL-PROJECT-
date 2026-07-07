package com.uce.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.auth_service.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void test_login_exitoso() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Mockito.when(authService.loginUser("user@example.com", "password123")).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    public void test_login_credenciales_invalidas() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        Mockito.when(authService.loginUser("user@example.com", "wrongpassword")).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    public void test_login_usuario_no_existe() throws Exception {
        AuthController.LoginRequest request = new AuthController.LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");

        Mockito.when(authService.loginUser("nonexistent@example.com", "password123")).thenReturn(null);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
    }

    @Test
    public void test_registro_exitoso() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nombre", "Test User");
        request.put("email", "newuser@example.com");
        request.put("password", "password123");
        request.put("rol", "ESTUDIANTE");

        Mockito.when(authService.registerUser("Test User", "newuser@example.com", "password123", "ESTUDIANTE"))
                .thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    public void test_registro_email_duplicado() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("nombre", "Test User");
        request.put("email", "existing@example.com");
        request.put("password", "password123");
        request.put("rol", "ESTUDIANTE");

        Mockito.when(authService.registerUser("Test User", "existing@example.com", "password123", "ESTUDIANTE"))
                .thenThrow(new RuntimeException("Email ya registrado"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email ya registrado"));
    }
}
