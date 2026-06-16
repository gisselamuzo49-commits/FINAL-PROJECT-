package com.uce.hours_service.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "horas_resumen")
public class HorasResumen {

    @Id
    private String estudianteId;
    private String nombre;
    private String carrera;
    private Double totalHorasValidadas = 0.0;
    private Double totalHorasPendientes = 0.0;
    private List<HistorialEntry> historial = new ArrayList<>();

    public HorasResumen() {}

    public HorasResumen(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public Double getTotalHorasValidadas() { return totalHorasValidadas; }
    public void setTotalHorasValidadas(Double totalHorasValidadas) { this.totalHorasValidadas = totalHorasValidadas; }

    public Double getTotalHorasPendientes() { return totalHorasPendientes; }
    public void setTotalHorasPendientes(Double totalHorasPendientes) { this.totalHorasPendientes = totalHorasPendientes; }

    public List<HistorialEntry> getHistorial() { return historial; }
    public void setHistorial(List<HistorialEntry> historial) { this.historial = historial; }
}
