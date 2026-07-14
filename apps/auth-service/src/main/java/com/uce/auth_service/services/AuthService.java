package com.uce.auth_service.services;

import com.uce.auth_service.models.User;
import com.uce.auth_service.models.Role;
import com.uce.auth_service.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(String nombre, String email, String password) {
        if (nombre == null || nombre.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Nombre, email y contraseña son obligatorios");
        }
        String normalizedName = nombre.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedName.length() > 255
                || !normalizedEmail.matches("^[^@\\s]+@uce\\.edu\\.ec$")
                || password.length() < 8
                || password.length() > 128) {
            throw new IllegalArgumentException("Datos de registro inválidos");
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("Email ya registrado");
        }
        Role rol = Role.ESTUDIANTE;
        User user = new User();
        user.setNombre(normalizedName);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRol(rol);
        userRepository.save(user);
        return generateToken(user);
    }

    @CircuitBreaker(name = "default", fallbackMethod = "loginUserFallback")
    public String loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuario no encontrado");
        }

        User user = userOpt.get();
        // 3. Verificar contraseña cifrada
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        // 4. Generar token JWT si es correcto
        return generateToken(user);
    }

    private String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId().toString())
                .claim("nombre", user.getNombre())
                .claim("rol", user.getRol().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // Expira en 1 día
                .signWith(key)
                .compact();
    }

    public String loginUserFallback(String email, String password, Throwable t) {
        return "FALLBACK_SERVICE_UNAVAILABLE";
    }
}
