package com.uce.linkage_service;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.services.LinkageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public LinkageProject createProject(@RequestBody LinkageProject project) {
        return linkageService.createProject(project);
    }

    @GetMapping
    public List<LinkageProject> getAllProjects() {
        return linkageService.getAllProjects();
    }

    @GetMapping("/{id}")
    public LinkageProject getProjectById(@PathVariable Long id) {
        return linkageService.getProjectById(id);
    }
}