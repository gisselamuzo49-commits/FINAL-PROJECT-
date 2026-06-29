package com.uce.linkage_service;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.services.LinkageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/linkage")
@Tag(name = "Vinculación", description = "Gestión de proyectos de vinculación con la sociedad de la UCE")
public class LinkageController {

    @Autowired
    private LinkageService linkageService;

    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Vinculación (Spring Boot)!";
    }

    @PostMapping
    @Operation(summary = "Crear proyecto de vinculación", description = "Registra un nuevo proyecto de vinculación con la sociedad, validando que el nombre y la institución sean proporcionados.")
    public ResponseEntity<?> createProject(@RequestBody LinkageProject project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre del proyecto es obligatorio.");
        }
        if (project.getInstitution() == null || project.getInstitution().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("La institución del proyecto es obligatoria.");
        }
        LinkageProject created = linkageService.createProject(project);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    @Operation(summary = "Listar todos los proyectos", description = "Retorna la lista completa de proyectos de vinculación con la sociedad registrados.")
    public ResponseEntity<List<LinkageProject>> getAllProjects() {
        return ResponseEntity.ok(linkageService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto por ID", description = "Retorna los detalles de un proyecto de vinculación específico por su identificador.")
    public ResponseEntity<LinkageProject> getProjectById(@PathVariable Long id) {
        Optional<LinkageProject> project = linkageService.getProjectById(id);
        return project.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }
}