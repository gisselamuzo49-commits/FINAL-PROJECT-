package com.uce.report_service.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "reportes_globales")
public class ReporteGlobal {

    @Id
    private String id = "global";

    private Integer totalEstudiantes;
    private BigDecimal totalHorasValidadas;
    private BigDecimal totalHorasPendientes;
    private Map<String, Integer> estudiantesPorFacultad = new HashMap<>();
    private LocalDateTime ultimaActualizacion;

    public ReporteGlobal() {
    }

    public ReporteGlobal(Integer totalEstudiantes, BigDecimal totalHorasValidadas, BigDecimal totalHorasPendientes, Map<String, Integer> estudiantesPorFacultad, LocalDateTime ultimaActualizacion) {
        this.totalEstudiantes = totalEstudiantes;
        this.totalHorasValidadas = totalHorasValidadas;
        this.totalHorasPendientes = totalHorasPendientes;
        this.estudiantesPorFacultad = estudiantesPorFacultad;
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTotalEstudiantes() {
        return totalEstudiantes;
    }

    public void setTotalEstudiantes(Integer totalEstudiantes) {
        this.totalEstudiantes = totalEstudiantes;
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

    public Map<String, Integer> getEstudiantesPorFacultad() {
        return estudiantesPorFacultad;
    }

    public void setEstudiantesPorFacultad(Map<String, Integer> estudiantesPorFacultad) {
        this.estudiantesPorFacultad = estudiantesPorFacultad;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }
}
