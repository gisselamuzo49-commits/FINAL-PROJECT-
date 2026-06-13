package com.uce.linkage_service;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.services.LinkageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/linkage")
public class LinkageController {

    @Autowired
    private LinkageService linkageService;

    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Vinculación (Spring Boot)!";
    }

    @PostMapping
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
    public ResponseEntity<List<LinkageProject>> getAllProjects() {
        return ResponseEntity.ok(linkageService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LinkageProject> getProjectById(@PathVariable Long id) {
        Optional<LinkageProject> project = linkageService.getProjectById(id);
        return project.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }
}