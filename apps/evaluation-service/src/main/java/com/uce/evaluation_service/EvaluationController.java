package com.uce.evaluation_service;

import com.uce.evaluation_service.dto.EvaluacionConEstudiante;
import com.uce.evaluation_service.models.EvaluacionFinal;
import com.uce.evaluation_service.services.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<?> createEvaluation(@RequestBody EvaluacionFinal evaluation) {
        if (evaluation.getEstudianteId() == null || evaluation.getEstudianteId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del estudiante es obligatorio.");
        }
        if (evaluation.getTutorId() == null || evaluation.getTutorId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del tutor es obligatorio.");
        }
        if (evaluation.getProyectoId() == null || evaluation.getProyectoId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El ID del proyecto es obligatorio.");
        }
        if (evaluation.getCalificacion() == null) {
            return ResponseEntity.badRequest().body("La calificación es obligatoria.");
        }

        // Validate that the rating is between 0 and 10 inclusive
        BigDecimal calificacion = evaluation.getCalificacion();
        if (calificacion.compareTo(BigDecimal.ZERO) < 0 || calificacion.compareTo(new BigDecimal("10")) > 0) {
            return ResponseEntity.badRequest().body("La calificación debe estar entre 0 y 10.");
        }

        EvaluacionFinal created = evaluationService.createEvaluation(evaluation);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluacionFinal> getEvaluationById(@PathVariable Long id) {
        Optional<EvaluacionFinal> evaluation = evaluationService.getEvaluationById(id);
        return evaluation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{estudianteId}")
    public ResponseEntity<List<EvaluacionConEstudiante>> getEvaluationsByStudentId(@PathVariable String estudianteId) {
        List<EvaluacionConEstudiante> evaluations = evaluationService.getEvaluationsByStudentId(estudianteId);
        return ResponseEntity.ok(evaluations);
    }
}
