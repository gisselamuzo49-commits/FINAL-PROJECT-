package com.uce.report_service.repositories;

import com.uce.report_service.models.ReporteEstudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReporteEstudianteRepository extends JpaRepository<ReporteEstudiante, Long> {
    Optional<ReporteEstudiante> findByEstudianteId(String estudianteId);
}
