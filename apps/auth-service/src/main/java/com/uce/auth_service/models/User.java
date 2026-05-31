package com.uce.auth_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // Usamos "users" en plural porque "user" a veces da problemas en SQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Esto hace que el ID sea automático (1, 2, 3...)
    private Long id;

    private String email;
    private String password;

    // 1. Constructor vacío (Obligatorio para que la base de datos funcione)
    public User() {
    }

    // 2. Constructor con datos (Para cuando nosotros queramos crear usuarios en el código)
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // 3. Getters y Setters (Las puertas para que Spring Boot pueda leer y escribir los datos)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}