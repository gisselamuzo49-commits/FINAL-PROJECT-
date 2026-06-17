package com.uce.report_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "registro_horas_reporte")
public class RegistroHorasReporte {

    @Id
    private String id; // Matches the registration UUID from the Kafka event

    @Column(name = "estudiante_id", nullable = false)
    private String estudianteId;

    @Column(nullable = false)
    private BigDecimal horas;

    @Column(nullable = false)
    private String estado; // PENDIENTE | VALIDADO | RECHAZADO

    public RegistroHorasReporte() {
    }

    public RegistroHorasReporte(String id, String estudianteId, BigDecimal horas, String estado) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.horas = horas;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
