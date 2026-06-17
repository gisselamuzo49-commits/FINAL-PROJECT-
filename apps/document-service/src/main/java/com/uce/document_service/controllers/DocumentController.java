package com.uce.document_service.controllers;

import com.uce.document_service.models.DocumentoGenerado;
import com.uce.document_service.models.DocumentoResumen;
import com.uce.document_service.repositories.DocumentoGeneradoRepository;
import com.uce.document_service.repositories.DocumentoResumenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentoGeneradoRepository postgresRepository;

    @Autowired
    private DocumentoResumenRepository mongoRepository;

    @GetMapping("/student/{estudianteId}")
    public ResponseEntity<DocumentoResumen> getDocumentResumenByStudent(@PathVariable String estudianteId) {
        if (estudianteId == null || estudianteId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<DocumentoResumen> resumen = mongoRepository.findById(estudianteId);
        return resumen.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoGenerado> getDocumentMetadataById(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<DocumentoGenerado> metadata = postgresRepository.findById(id);
        return metadata.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
