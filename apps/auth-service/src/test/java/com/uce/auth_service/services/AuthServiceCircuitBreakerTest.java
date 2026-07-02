package com.uce.auth_service.services;

import com.uce.auth_service.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AuthServiceCircuitBreakerTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    public void testLoginUserFallback_WhenRepositoryThrowsException() {
        // Simular que el repositorio arroja un error al buscar por email
        Mockito.when(userRepository.findByEmail(Mockito.anyString()))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // Ejecutar el método que tiene la anotación @CircuitBreaker
        String token = authService.loginUser("test@test.com", "password");

        // Verificar que el fallback es invocado y retorna el string esperado
        assertEquals("FALLBACK_SERVICE_UNAVAILABLE", token);
    }
}
