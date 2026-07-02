package com.uce.internship_service.controllers;

import com.uce.internship_service.repositories.EstudianteAplicanteDto;
import com.uce.internship_service.repositories.StudentApplicationDto;
import com.uce.internship_service.services.PostulacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/internships")
@Tag(name = "Postulaciones", description = "Gestión de solicitudes y postulaciones de estudiantes a ofertas")
public class PostulacionController {

    @Autowired
    private PostulacionService postulacionService;

    @PostMapping("/{internshipId}/applications")
    @Operation(summary = "Crear postulación", description = "Registra la postulación de un estudiante a una oferta de pasantía específica.")
    public StudentApplicationDto createApplication(
            @PathVariable Long internshipId,
            @RequestBody ApplicationRequest request) {
        return postulacionService.createApplication(internshipId, request.getEstudianteId(), request.getMensaje());
    }

    @GetMapping("/{internshipId}/applications")
    @Operation(summary = "Listar aplicantes de una oferta", description = "Retorna la lista de estudiantes que han postulado a una oferta de pasantía específica.")
    public List<EstudianteAplicanteDto> getApplicantsForInternship(
            @PathVariable Long internshipId) {
        return postulacionService.getApplicantsForInternship(internshipId);
    }

    @GetMapping("/applications/student/{estudianteId}")
    @Operation(summary = "Listar postulaciones de un estudiante", description = "Retorna el historial de ofertas de pasantía a las que ha postulado un estudiante determinado.")
    public List<StudentApplicationDto> getApplicationsForStudent(
            @PathVariable String estudianteId) {
        return postulacionService.getApplicationsForStudent(estudianteId);
    }

    @PatchMapping("/applications/{postulacionId}/status")
    @Operation(summary = "Actualizar estado de postulación", description = "Actualiza el estado de una postulación (ej. ACEPTADA, RECHAZADA) por parte de un tutor o coordinador.")
    @PreAuthorize("hasAnyRole('TUTOR', 'COORDINADOR')")
    public StudentApplicationDto updateStatus(
            @PathVariable Long postulacionId,
            @RequestBody StatusUpdateRequest request) {
        return postulacionService.updateStatus(postulacionId, request.getEstado());
    }
}
