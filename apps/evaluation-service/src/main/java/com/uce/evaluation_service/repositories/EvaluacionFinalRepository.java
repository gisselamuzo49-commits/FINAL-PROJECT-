package com.uce.evaluation_service.repositories;

import com.uce.evaluation_service.models.EvaluacionFinal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionFinalRepository extends JpaRepository<EvaluacionFinal, Long> {
    List<EvaluacionFinal> findByEstudianteId(String estudianteId);
}
