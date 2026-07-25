package com.uce.hours_service.cqrs.commands;

import java.time.LocalDate;

public final class CreateHoursCommand {
    private final String estudianteId;
    private final String proyectoId;
    private final LocalDate fecha;
    private final Double horas;
    private final String descripcionActividad;

    public CreateHoursCommand(String estudianteId, String proyectoId, LocalDate fecha, Double horas, String descripcionActividad) {
        this.estudianteId = estudianteId;
        this.proyectoId = proyectoId;
        this.fecha = fecha;
        this.horas = horas;
        this.descripcionActividad = descripcionActividad;
    }

    public String getEstudianteId() { return estudianteId; }
    public String getProyectoId() { return proyectoId; }
    public LocalDate getFecha() { return fecha; }
    public Double getHoras() { return horas; }
    public String getDescripcionActividad() { return descripcionActividad; }
}
