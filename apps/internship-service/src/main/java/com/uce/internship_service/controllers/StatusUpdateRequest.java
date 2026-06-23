package com.uce.internship_service.controllers;

public class StatusUpdateRequest {
    private String estado;

    public StatusUpdateRequest() {}

    public StatusUpdateRequest(String estado) {
        this.estado = estado;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
