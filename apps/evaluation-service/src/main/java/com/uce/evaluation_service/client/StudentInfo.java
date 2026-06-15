package com.uce.evaluation_service.client;

public class StudentInfo {
    private String nombre;
    private String apellido;
    private String carrera;

    public StudentInfo() {}

    public StudentInfo(String nombre, String apellido, String carrera) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.carrera = carrera;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
}
