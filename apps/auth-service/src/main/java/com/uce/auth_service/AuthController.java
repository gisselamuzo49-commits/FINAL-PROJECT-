package com.uce.auth_service;

import com.uce.auth_service.models.User;
import com.uce.auth_service.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión de usuarios")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Pasantías (Spring Boot)!";
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service is running");
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario", description = "Crea una cuenta para un Estudiante, Tutor o Coordinador y devuelve su JWT correspondiente.")
    public ResponseEntity<Map<String, String>> register(@RequestBody Map<String, String> body) {
        String token = authService.registerUser(
            body.get("nombre"),
            body.get("email"), 
            body.get("password"),
            body.get("rol")
        );
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario por email y contraseña y devuelve su token JWT.")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
        if (token == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("email", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    // DTO para la petición de Login
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}