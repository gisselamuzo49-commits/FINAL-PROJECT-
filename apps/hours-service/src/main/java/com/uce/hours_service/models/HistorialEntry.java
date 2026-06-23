package com.uce.hours_service.models;

public class HistorialEntry {
    private String registroId;
    private String proyectoId;
    private String fecha;
    private Double horas;
    private String estado;
    private String descripcionActividad;

    public HistorialEntry() {}

    public HistorialEntry(String registroId, String proyectoId, String fecha, Double horas, String estado, String descripcionActividad) {
        this.registroId = registroId;
        this.proyectoId = proyectoId;
        this.fecha = fecha;
        this.horas = horas;
        this.estado = estado;
        this.descripcionActividad = descripcionActividad;
    }

    public String getRegistroId() { return registroId; }
    public void setRegistroId(String registroId) { this.registroId = registroId; }

    public String getProyectoId() { return proyectoId; }
    public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public Double getHoras() { return horas; }
    public void setHoras(Double horas) { this.horas = horas; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDescripcionActividad() { return descripcionActividad; }
    public void setDescripcionActividad(String descripcionActividad) { this.descripcionActividad = descripcionActividad; }
}
