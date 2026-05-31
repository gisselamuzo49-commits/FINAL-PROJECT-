package com.uce.internship_service.controllers;

import com.uce.internship_service.models.Internship;
import com.uce.internship_service.services.InternshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
@CrossOrigin(origins = "http://localhost:5173") // Permitimos que React se conecte
public class InternshipController {

    @Autowired
    private InternshipService internshipService;

    // Ruta para GUARDAR (POST)
    @PostMapping
    public Internship create(@RequestBody Internship internship) {
        return internshipService.createInternship(internship);
    }

    // Ruta para LEER (GET)
    @GetMapping
    public List<Internship> getAll() {
        return internshipService.getAllInternships();
    }
}
