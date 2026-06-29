package com.uce.internship_service.controllers;

import com.uce.internship_service.models.Internship;
import com.uce.internship_service.services.InternshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
@Tag(name = "Pasantías", description = "Catálogo y gestión de ofertas de pasantías preprofesionales")
public class InternshipController {

    @Autowired
    private InternshipService internshipService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Internship Service is running");
    }

    // Ruta para GUARDAR (POST)
    @PostMapping
    @Operation(summary = "Crear una nueva oferta", description = "Publica una nueva oferta de pasantía preprofesional en el catálogo.")
    public Internship create(@RequestBody Internship internship) {
        return internshipService.createInternship(internship);
    }

    // Ruta para LEER (GET)
    @GetMapping
    @Operation(summary = "Listar todas las ofertas", description = "Retorna el catálogo completo de ofertas de pasantías preprofesionales publicadas.")
    public List<Internship> getAll() {
        return internshipService.getAllInternships();
    }
}
