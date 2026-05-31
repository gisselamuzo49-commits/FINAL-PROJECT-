package com.uce.internship_service.services;

import com.uce.internship_service.models.Internship;
import com.uce.internship_service.repositories.InternshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InternshipService {

    @Autowired
    private InternshipRepository internshipRepository;

    // Función para crear una nueva oferta
    public Internship createInternship(Internship internship) {
        return internshipRepository.save(internship);
    }

    // Función para obtener todas las ofertas disponibles
    public List<Internship> getAllInternships() {
        return internshipRepository.findAll();
    }
}
