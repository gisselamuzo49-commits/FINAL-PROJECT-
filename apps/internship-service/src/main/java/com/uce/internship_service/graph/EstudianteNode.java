package com.uce.internship_service.graph;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;

@Node("Estudiante")
public class EstudianteNode {

    @Id
    private String estudianteId;

    @Relationship(type = "POSTULA_A", direction = Relationship.Direction.OUTGOING)
    private Set<Postulacion> postulaciones = new HashSet<>();

    public EstudianteNode() {}

    public EstudianteNode(String estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

    public Set<Postulacion> getPostulaciones() { return postulaciones; }
    public void setPostulaciones(Set<Postulacion> postulaciones) { this.postulaciones = postulaciones; }

    public void addPostulacion(Postulacion postulacion) {
        this.postulaciones.add(postulacion);
    }
}
