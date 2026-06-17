package com.uce.report_service.repositories;

import com.uce.report_service.models.RegistroHorasReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroHorasReporteRepository extends JpaRepository<RegistroHorasReporte, String> {
    List<RegistroHorasReporte> findByEstudianteId(String estudianteId);
}
