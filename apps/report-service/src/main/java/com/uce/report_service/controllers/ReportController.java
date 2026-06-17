package com.uce.report_service.controllers;

import com.uce.report_service.models.ReporteEstudiante;
import com.uce.report_service.models.ReporteGlobal;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import com.uce.report_service.repositories.ReporteGlobalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReporteEstudianteRepository postgresRepository;

    @Autowired
    private ReporteGlobalRepository mongoRepository;

    @GetMapping("/student/{estudianteId}")
    public ResponseEntity<ReporteEstudiante> getStudentReport(@PathVariable String estudianteId) {
        if (estudianteId == null || estudianteId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<ReporteEstudiante> report = postgresRepository.findByEstudianteId(estudianteId);
        return report.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/global")
    public ResponseEntity<ReporteGlobal> getGlobalReport() {
        Optional<ReporteGlobal> globalReport = mongoRepository.findById("global");
        return globalReport.map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
