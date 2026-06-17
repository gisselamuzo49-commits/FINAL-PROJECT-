package com.uce.document_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.document_service.services.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaDocumentConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDocumentConsumer.class);

    @Autowired
    private DocumentService documentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "horas.registradas", groupId = "document-service-group")
    public void consume(String message) {
        logger.info("Evento recibido de Kafka en topic horas.registradas: {}", message);
        try {
            HorasRegistradasEvent event = objectMapper.readValue(message, HorasRegistradasEvent.class);
            if (event.getEstudianteId() == null || event.getId() == null) {
                logger.warn("El evento recibido tiene id o estudianteId nulo. Ignorando...");
                return;
            }

            String estado = event.getEstado();
            if ("VALIDADO".equalsIgnoreCase(estado)) {
                logger.info("El estado es VALIDADO. Iniciando generación de documento para el estudiante: {}", event.getEstudianteId());
                documentService.generateAndUploadDocument(
                        event.getEstudianteId(),
                        event.getProyectoId(),
                        event.getHoras(),
                        event.getFecha(),
                        event.getId()
                );
            } else {
                logger.info("El estado es '{}'. No requiere generación de documentos.", estado);
            }

        } catch (Exception e) {
            logger.error("Error al procesar y deserializar evento Kafka: {}", e.getMessage(), e);
        }
    }

    public static class HorasRegistradasEvent {
        private Long id;
        private String estudianteId;
        private String proyectoId;
        private String fecha;
        private Double horas;
        private String descripcionActividad;
        private String estado;
        private String tutorId;
        private String fechaValidacion;

        // Getters y Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEstudianteId() { return estudianteId; }
        public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }

        public String getProyectoId() { return proyectoId; }
        public void setProyectoId(String proyectoId) { this.proyectoId = proyectoId; }

        public String getFecha() { return fecha; }
        public void setFecha(String fecha) { this.fecha = fecha; }

        public Double getHoras() { return horas; }
        public void setHoras(Double horas) { this.horas = horas; }

        public String getDescripcionActividad() { return descripcionActividad; }
        public void setDescripcionActividad(String descripcionActividad) { this.descripcionActividad = descripcionActividad; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }

        public String getTutorId() { return tutorId; }
        public void setTutorId(String tutorId) { this.tutorId = tutorId; }

        public String getFechaValidacion() { return fechaValidacion; }
        public void setFechaValidacion(String fechaValidacion) { this.fechaValidacion = fechaValidacion; }
    }
}
