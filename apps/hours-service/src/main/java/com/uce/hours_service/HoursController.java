package com.uce.hours_service;

import com.uce.hours_service.models.RegistroHoras;
import com.uce.hours_service.models.HorasResumen;
import com.uce.hours_service.services.HoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/hours")
public class HoursController {

    @Autowired
    private HoursService hoursService;

    @GetMapping("/student/{estudianteId}")
    public ResponseEntity<?> getStudentSummary(@PathVariable String estudianteId) {
        return hoursService.getStudentSummary(estudianteId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createHoursRegistration(@RequestBody RegistroHoras registro) {
        if (registro.getEstudianteId() == null || registro.getEstudianteId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del estudiante es obligatorio.");
        }
        if (registro.getProyectoId() == null || registro.getProyectoId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del proyecto es obligatorio.");
        }
        if (registro.getFecha() == null) {
            return ResponseEntity.badRequest().body("La fecha es obligatoria.");
        }
        if (registro.getHoras() == null) {
            return ResponseEntity.badRequest().body("El numero de horas es obligatorio.");
        }

        RegistroHoras created = hoursService.createHoursRegistration(registro);
        return ResponseEntity.ok(created);
    }

    @PatchMapping("/{id}/validar")
    @PreAuthorize("hasAnyRole('TUTOR', 'COORDINADOR')")
    public ResponseEntity<?> validarHoursRegistration(
            @PathVariable Long id,
            @RequestBody ValidarRequest request) {

        if (request.getTutorId() == null || request.getTutorId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del tutor es obligatorio.");
        }
        if (request.getAprobado() == null) {
            return ResponseEntity.badRequest().body("El campo aprobado es obligatorio.");
        }

        Optional<RegistroHoras> updatedOpt = hoursService.validarHoursRegistration(
                id, request.getTutorId(), request.getAprobado());

        if (updatedOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedOpt.get());
    }

    public static class ValidarRequest {
        private String tutorId;
        private Boolean aprobado;

        public String getTutorId() { return tutorId; }
        public void setTutorId(String tutorId) { this.tutorId = tutorId; }

        public Boolean getAprobado() { return aprobado; }
        public void setAprobado(Boolean aprobado) { this.aprobado = aprobado; }
    }
}
