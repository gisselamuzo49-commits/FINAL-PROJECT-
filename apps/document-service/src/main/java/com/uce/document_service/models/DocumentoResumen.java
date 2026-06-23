package com.uce.document_service.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "documentos_resumen")
public class DocumentoResumen {

    @Id
    private String id; // Represents estudianteId

    private Integer totalDocumentos = 0;

    private List<DocumentInfo> documentos = new ArrayList<>();

    public DocumentoResumen() {}

    public DocumentoResumen(String estudianteId) {
        this.id = estudianteId;
        this.totalDocumentos = 0;
        this.documentos = new ArrayList<>();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getTotalDocumentos() { return totalDocumentos; }
    public void setTotalDocumentos(Integer totalDocumentos) { this.totalDocumentos = totalDocumentos; }

    public List<DocumentInfo> getDocumentos() { return documentos; }
    public void setDocumentos(List<DocumentInfo> documentos) { this.documentos = documentos; }

    public static class DocumentInfo {
        private Long documentoId;
        private String tipo;
        private String s3Url;
        private LocalDateTime createdAt;

        public DocumentInfo() {}

        public DocumentInfo(Long documentoId, String tipo, String s3Url, LocalDateTime createdAt) {
            this.documentoId = documentoId;
            this.tipo = tipo;
            this.s3Url = s3Url;
            this.createdAt = createdAt;
        }

        // Getters y Setters
        public Long getDocumentoId() { return documentoId; }
        public void setDocumentoId(Long documentoId) { this.documentoId = documentoId; }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public String getS3Url() { return s3Url; }
        public void setS3Url(String s3Url) { this.s3Url = s3Url; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
