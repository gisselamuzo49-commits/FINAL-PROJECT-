package com.uce.auth_service.services;

import com.uce.auth_service.models.User;
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
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String registerUser(User user) {
        // 1. Revisar si el correo ya existe
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Error: El correo ya está registrado en el sistema.";
        }
        
        // 2. Cifrar la contraseña con BCrypt antes de guardar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "¡Éxito! Usuario registrado en la base de datos.";
    }

    public String loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null; // Usuario no encontrado
        }

        User user = userOpt.get();
        // 3. Verificar contraseña cifrada
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null; // Contraseña incorrecta
        }

        // 4. Generar token JWT si es correcto
        return generateToken(user.getEmail());
    }

    private String generateToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // Expira en 1 día
                .signWith(key)
                .compact();
    }
}