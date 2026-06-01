package com.uce.auth_service.repositories;

import com.uce.auth_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Esta línea es magia pura: Spring Boot creará automáticamente el código SQL 
    // para buscar un usuario por su correo electrónico.
    Optional<User> findByEmail(String email);
    
}