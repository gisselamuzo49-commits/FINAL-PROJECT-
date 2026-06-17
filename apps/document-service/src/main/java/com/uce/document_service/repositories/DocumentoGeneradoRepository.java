package com.uce.document_service.repositories;

import com.uce.document_service.models.DocumentoGenerado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoGeneradoRepository extends JpaRepository<DocumentoGenerado, Long> {
    List<DocumentoGenerado> findByEstudianteIdOrderByCreatedAtDesc(String estudianteId);
}
