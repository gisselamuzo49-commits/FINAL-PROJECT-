package com.uce.evaluation_service.services;

import com.uce.evaluation_service.client.StudentInfo;
import com.uce.evaluation_service.client.UserServiceClient;
import com.uce.evaluation_service.dto.EvaluacionConEstudiante;
import com.uce.evaluation_service.models.EvaluacionFinal;
import com.uce.evaluation_service.repositories.EvaluacionFinalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private EvaluacionFinalRepository repository;

    @Autowired
    private UserServiceClient userServiceClient;

    public EvaluacionFinal createEvaluation(EvaluacionFinal evaluation) {
        return repository.save(evaluation);
    }

    public Optional<EvaluacionFinal> getEvaluationById(Long id) {
        return repository.findById(id);
    }

    public List<EvaluacionConEstudiante> getEvaluationsByStudentId(String estudianteId) {
        List<EvaluacionFinal> evaluations = repository.findByEstudianteId(estudianteId);

        // Fetch student info once using gRPC to avoid redundant network calls
        Optional<StudentInfo> studentInfoOpt = userServiceClient.getStudentInfo(estudianteId);

        String nombreCompleto = null;
        String carrera = null;
        if (studentInfoOpt.isPresent()) {
            StudentInfo info = studentInfoOpt.get();
            String nombre = info.getNombre() != null ? info.getNombre().trim() : "";
            String apellido = info.getApellido() != null ? info.getApellido().trim() : "";
            if (!nombre.isEmpty() && !apellido.isEmpty()) {
                nombreCompleto = nombre + " " + apellido;
            } else if (!nombre.isEmpty()) {
                nombreCompleto = nombre;
            } else {
                nombreCompleto = apellido;
            }
            carrera = info.getCarrera();
        }

        final String finalNombre = nombreCompleto;
        final String finalCarrera = carrera;

        return evaluations.stream()
                .map(eval -> new EvaluacionConEstudiante(eval, finalNombre, finalCarrera))
                .collect(Collectors.toList());
    }
}
