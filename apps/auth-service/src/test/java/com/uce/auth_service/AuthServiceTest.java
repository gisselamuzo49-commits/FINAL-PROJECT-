package com.uce.auth_service;

import com.uce.auth_service.models.Role;
import com.uce.auth_service.models.User;
import com.uce.auth_service.repositories.UserRepository;
import com.uce.auth_service.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(authService, "jwtSecret", "my-super-secret-key-that-is-at-least-256-bits-long-for-hmac");
    }

    @Test
    public void test_loginUser_retorna_token() {
        User user = new User();
        user.setId(1L);
        user.setNombre("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRol(Role.ESTUDIANTE);

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        String token = authService.loginUser("test@example.com", "password123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void test_loginUser_password_incorrecto() {
        User user = new User();
        user.setId(1L);
        user.setNombre("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRol(Role.ESTUDIANTE);

        Mockito.when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> {
            authService.loginUser("test@example.com", "wrongpassword");
        });
    }

    @Test
    public void test_registerUser_exitoso() {
        Mockito.when(userRepository.findByEmail("new@uce.edu.ec")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        String token = authService.registerUser("New User", "new@uce.edu.ec", "password123");
        assertNotNull(token);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.ESTUDIANTE, userCaptor.getValue().getRol());
    }
}
