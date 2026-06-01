package com.uce.internship_service.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "internships")
public class Internship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String description;
    private String status; // Ej: "ABIERTA", "CERRADA"

    // 1. Constructor vacío (Obligatorio para Spring Boot)
    public Internship() {
    }

    // 2. Constructor con datos
    public Internship(String title, String company, String description, String status) {
        this.title = title;
        this.company = company;
        this.description = description;
        this.status = status;
    }

    // 3. Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
