package com.uce.auth_service.services;

import com.uce.auth_service.models.User;
import com.uce.auth_service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    public String registerUser(User user) {
        // Regla 1: Revisar si el correo ya existe
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "Error: El correo ya está registrado en el sistema.";
        }
        
        // Regla 2: Si no existe, lo guardamos en la base de datos
        userRepository.save(user);
        return "¡Éxito! Usuario registrado en la base de datos.";
    }
}