package com.uce.document_service.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documento")
public class DocumentoGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false)
    private String estudianteId;

    @Column(name = "proyecto_id", nullable = false)
    private String proyectoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoDocumento tipo;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "s3_url", length = 1000)
    private String s3Url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoDocumento estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public DocumentoGenerado() {}

    public DocumentoGenerado(String estudianteId, String proyectoId, TipoDocumento tipo, String s3Key, String s3Url, EstadoDocumento estado) {
        this.estudianteId = estudianteId;
        this.proyectoId = proyectoId;
        this.tipo = tipo;
        this.s3Key = s3Key;
        this.s3Url = s3Url;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public String getProyectoId() { return proyectoId; }
    public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

    public TipoDocumento getTipo() { return tipo; }
    public void setTipo(TipoDocumento tipo) { this.tipo = tipo; }

    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }

    public String getS3Url() { return s3Url; }
    public void setS3Url(String s3Url) { this.s3Url = s3Url; }

    public EstadoDocumento getEstado() { return estado; }
    public void setEstado(EstadoDocumento estado) { this.estado = estado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
