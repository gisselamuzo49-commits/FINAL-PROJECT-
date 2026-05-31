package com.uce.auth_service;

import com.uce.auth_service.models.User;
import com.uce.auth_service.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private AuthService authService;

    // Nuestra puerta de prueba anterior
    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Pasantías (Spring Boot)!";
    }

    // ¡NUEVA PUERTA! Recibe datos de un usuario y los manda al servicio para guardarlos
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return authService.registerUser(user);
    }
}