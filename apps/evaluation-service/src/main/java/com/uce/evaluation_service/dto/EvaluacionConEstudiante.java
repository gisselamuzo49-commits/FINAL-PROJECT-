package com.uce.evaluation_service.dto;

import com.uce.evaluation_service.models.EvaluacionFinal;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EvaluacionConEstudiante {
    private Long id;
    private String estudianteId;
    private String proyectoId;
    private String tutorId;
    private LocalDate fechaEvaluacion;
    private BigDecimal calificacion;
    private String comentarios;
    private LocalDateTime createdAt;
    private String nombre;
    private String carrera;

    public EvaluacionConEstudiante() {}

    public EvaluacionConEstudiante(EvaluacionFinal eval, String nombre, String carrera) {
        this.id = eval.getId();
        this.estudianteId = eval.getEstudianteId();
        this.proyectoId = eval.getProyectoId();
        this.tutorId = eval.getTutorId();
        this.fechaEvaluacion = eval.getFechaEvaluacion();
        this.calificacion = eval.getCalificacion();
        this.comentarios = eval.getComentarios();
        this.createdAt = eval.getCreatedAt();
        this.nombre = nombre;
        this.carrera = carrera;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public String getProyectoId() { return proyectoId; }
    public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

    public String getTutorId() { return tutorId; }
    public void setTutorId(String tutorId) { this.tutorId = tutorId; }

    public LocalDate getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDate fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }

    public BigDecimal getCalificacion() { return calificacion; }
    public void setCalificacion(BigDecimal calificacion) { this.calificacion = calificacion; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
}
