package com.uce.auth_service;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // ¡Esto es vital para que acepte llamadas de tu React!
public class AuthController {

    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Pasantías (Spring Boot)!";
    }
}