package com.uce.internship_service.controllers;

public class ApplicationRequest {
    private String estudianteId;
    private String mensaje;

    public ApplicationRequest() {}

    public ApplicationRequest(String estudianteId, String mensaje) {
        this.estudianteId = estudianteId;
        this.mensaje = mensaje;
    }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
