package com.uce.report_service.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_estudiante")
public class ReporteEstudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estudiante_id", nullable = false, unique = true)
    private String estudianteId;

    @Column(name = "total_horas_validadas", nullable = false)
    private BigDecimal totalHorasValidadas;

    @Column(name = "total_horas_pendientes", nullable = false)
    private BigDecimal totalHorasPendientes;

    @Column(name = "total_documentos", nullable = false)
    private Integer totalDocumentos;

    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    public ReporteEstudiante() {
    }

    public ReporteEstudiante(String estudianteId, BigDecimal totalHorasValidadas, BigDecimal totalHorasPendientes, Integer totalDocumentos, LocalDateTime ultimaActualizacion) {
        this.estudianteId = estudianteId;
        this.totalHorasValidadas = totalHorasValidadas;
        this.totalHorasPendientes = totalHorasPendientes;
        this.totalDocumentos = totalDocumentos;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public BigDecimal getTotalHorasValidadas() {
        return totalHorasValidadas;
    }

    public void setTotalHorasValidadas(BigDecimal totalHorasValidadas) {
        this.totalHorasValidadas = totalHorasValidadas;
    }

    public BigDecimal getTotalHorasPendientes() {
        return totalHorasPendientes;
    }

    public void setTotalHorasPendientes(BigDecimal totalHorasPendientes) {
        this.totalHorasPendientes = totalHorasPendientes;
    }

    public Integer getTotalDocumentos() {
        return totalDocumentos;
    }

    public void setTotalDocumentos(Integer totalDocumentos) {
        this.totalDocumentos = totalDocumentos;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}
