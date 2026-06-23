package com.uce.internship_service.graph;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class Postulacion {

    @RelationshipId
    private Long id;

    private String estado; // Ej: "PENDIENTE", "ACEPTADA", "RECHAZADA"
    private String mensaje;
    private LocalDateTime fechaPostulacion;

    @TargetNode
    private OfertaNode oferta;

    public Postulacion() {}

    public Postulacion(String estado, String mensaje, LocalDateTime fechaPostulacion, OfertaNode oferta) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.fechaPostulacion = fechaPostulacion;
        this.oferta = oferta;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDateTime fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public OfertaNode getOferta() { return oferta; }
    public void setOferta(OfertaNode oferta) { this.oferta = oferta; }
}
