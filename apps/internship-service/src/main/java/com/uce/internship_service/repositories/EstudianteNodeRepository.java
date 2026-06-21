package com.uce.internship_service.repositories;

import com.uce.internship_service.graph.EstudianteNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteNodeRepository extends Neo4jRepository<EstudianteNode, String> {

    @Query("MATCH (e:Estudiante {estudianteId: $estudianteId})-[r:POSTULA_A]->(o:Oferta {internshipId: $internshipId}) RETURN count(r) > 0")
    boolean existsApplication(String estudianteId, String internshipId);

    @Query("MATCH (e:Estudiante)-[r:POSTULA_A]->(o:Oferta {internshipId: $internshipId}) " +
           "RETURN e.estudianteId AS estudianteId, id(r) AS postulacionId, " +
           "r.estado AS estado, r.mensaje AS mensaje, r.fechaPostulacion AS fechaPostulacion")
    List<EstudianteAplicanteDto> findApplicantsByInternshipId(String internshipId);

    @Query("MATCH (e:Estudiante {estudianteId: $estudianteId})-[r:POSTULA_A]->(o:Oferta) " +
           "RETURN o.internshipId AS internshipId, o.title AS title, o.company AS company, " +
           "id(r) AS postulacionId, r.estado AS estado, r.mensaje AS mensaje, r.fechaPostulacion AS fechaPostulacion")
    List<StudentApplicationDto> findApplicationsByEstudianteId(String estudianteId);

    @Query("MATCH ()-[r:POSTULA_A]->() WHERE id(r) = $postulacionId RETURN count(r) > 0")
    boolean existsPostulacionById(Long postulacionId);

    @Query("MATCH ()-[r:POSTULA_A]->() WHERE id(r) = $postulacionId SET r.estado = $estado")
    void updatePostulacionStatus(Long postulacionId, String estado);

    @Query("MATCH (e:Estudiante)-[r:POSTULA_A]->(o:Oferta) WHERE id(r) = $postulacionId " +
           "RETURN e.estudianteId AS estudianteId, o.internshipId AS internshipId, " +
           "o.title AS title, o.company AS company, id(r) AS postulacionId, " +
           "r.estado AS estado, r.mensaje AS mensaje, r.fechaPostulacion AS fechaPostulacion")
    Optional<StudentApplicationDto> findApplicationByPostulacionId(Long postulacionId);
}
