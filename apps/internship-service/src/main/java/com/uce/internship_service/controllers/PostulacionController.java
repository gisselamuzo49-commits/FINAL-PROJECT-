package com.uce.internship_service.controllers;

import com.uce.internship_service.repositories.EstudianteAplicanteDto;
import com.uce.internship_service.repositories.StudentApplicationDto;
import com.uce.internship_service.services.PostulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internships")
public class PostulacionController {

    @Autowired
    private PostulacionService postulacionService;

    @PostMapping("/{internshipId}/applications")
    public StudentApplicationDto createApplication(
            @PathVariable Long internshipId,
            @RequestBody ApplicationRequest request) {
        return postulacionService.createApplication(internshipId, request.getEstudianteId(), request.getMensaje());
    }

    @GetMapping("/{internshipId}/applications")
    public List<EstudianteAplicanteDto> getApplicantsForInternship(
            @PathVariable Long internshipId) {
        return postulacionService.getApplicantsForInternship(internshipId);
    }

    @GetMapping("/applications/student/{estudianteId}")
    public List<StudentApplicationDto> getApplicationsForStudent(
            @PathVariable String estudianteId) {
        return postulacionService.getApplicationsForStudent(estudianteId);
    }

    @PatchMapping("/applications/{postulacionId}/status")
    public StudentApplicationDto updateStatus(
            @PathVariable Long postulacionId,
            @RequestBody StatusUpdateRequest request) {
        return postulacionService.updateStatus(postulacionId, request.getEstado());
    }
}
