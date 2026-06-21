package com.uce.internship_service.services;

import com.uce.internship_service.graph.EstudianteNode;
import com.uce.internship_service.graph.OfertaNode;
import com.uce.internship_service.graph.Postulacion;
import com.uce.internship_service.models.Internship;
import com.uce.internship_service.repositories.EstudianteAplicanteDto;
import com.uce.internship_service.repositories.EstudianteNodeRepository;
import com.uce.internship_service.repositories.InternshipRepository;
import com.uce.internship_service.repositories.OfertaNodeRepository;
import com.uce.internship_service.repositories.StudentApplicationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostulacionService {

    @Autowired
    private InternshipRepository internshipRepository;

    @Autowired
    private EstudianteNodeRepository estudianteNodeRepository;

    @Autowired
    private OfertaNodeRepository ofertaNodeRepository;

    @Transactional("neo4jTransactionManager")
    public StudentApplicationDto createApplication(Long internshipId, String estudianteId, String mensaje) {
        // 1. Validar en PostgreSQL que la oferta existe
        Internship internship = internshipRepository.findById(internshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no encontrada con id: " + internshipId));

        // 2. Validar que el estudiante no haya postulado ya
        String internshipIdStr = String.valueOf(internshipId);
        if (estudianteNodeRepository.existsApplication(estudianteId, internshipIdStr)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya has postulado a esta oferta");
        }

        // 3. Obtener o crear OfertaNode en Neo4j
        OfertaNode ofertaNode = ofertaNodeRepository.findById(internshipIdStr)
                .orElseGet(() -> ofertaNodeRepository.save(new OfertaNode(internshipIdStr, internship.getTitle(), internship.getCompany())));

        // 4. Obtener o crear EstudianteNode en Neo4j
        EstudianteNode estudianteNode = estudianteNodeRepository.findById(estudianteId)
                .orElseGet(() -> new EstudianteNode(estudianteId));

        // 5. Crear relación
        Postulacion postulacion = new Postulacion("PENDIENTE", mensaje, LocalDateTime.now(), ofertaNode);
        estudianteNode.addPostulacion(postulacion);

        estudianteNodeRepository.save(estudianteNode);

        // 6. Recuperar la proyección de la aplicación creada (que contiene el ID de la relación auto-generado)
        return estudianteNodeRepository.findApplicationsByEstudianteId(estudianteId).stream()
                .filter(app -> app.internshipId().equals(internshipIdStr))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear la postulación"));
    }

    public List<EstudianteAplicanteDto> getApplicantsForInternship(Long internshipId) {
        // Validar que la oferta existe en PostgreSQL
        if (!internshipRepository.existsById(internshipId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no encontrada con id: " + internshipId);
        }
        return estudianteNodeRepository.findApplicantsByInternshipId(String.valueOf(internshipId));
    }

    public List<StudentApplicationDto> getApplicationsForStudent(String estudianteId) {
        return estudianteNodeRepository.findApplicationsByEstudianteId(estudianteId);
    }

    @Transactional("neo4jTransactionManager")
    public StudentApplicationDto updateStatus(Long postulacionId, String estado) {
        if (!estudianteNodeRepository.existsPostulacionById(postulacionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Postulación no encontrada con id: " + postulacionId);
        }
        if (!"ACEPTADA".equals(estado) && !"RECHAZADA".equals(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido. Debe ser ACEPTADA o RECHAZADA");
        }
        estudianteNodeRepository.updatePostulacionStatus(postulacionId, estado);
        return estudianteNodeRepository.findApplicationByPostulacionId(postulacionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar el estado de la postulación"));
    }
}
