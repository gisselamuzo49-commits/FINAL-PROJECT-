package com.uce.notification_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.models.TipoNotificacion;
import com.uce.notification_service.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaNotificationConsumer.class);

    @Autowired
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "horas.registradas", groupId = "notification-service-group")
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
                String mensaje = "Se han validado sus horas para el proyecto " + event.getProyectoId()
                        + " el " + event.getFechaValidacion() + " por el tutor " + event.getTutorId() + ".";
                Notificacion notificacion = new Notificacion(
                        event.getEstudianteId(),
                        mensaje,
                        TipoNotificacion.HORAS_VALIDADAS,
                        event.getId()
                );
                notificationService.createNotification(notificacion);
                logger.info("Notificación de validación de horas guardada y publicada para el estudiante: {}", event.getEstudianteId());
            } else if ("RECHAZADO".equalsIgnoreCase(estado)) {
                String mensaje = "Se han rechazado sus horas para el proyecto " + event.getProyectoId() + ".";
                Notificacion notificacion = new Notificacion(
                        event.getEstudianteId(),
                        mensaje,
                        TipoNotificacion.HORAS_RECHAZADAS,
                        event.getId()
                );
                notificationService.createNotification(notificacion);
                logger.info("Notificación de rechazo de horas guardada y publicada para el estudiante: {}", event.getEstudianteId());
            } else {
                logger.info("El estado del registro de horas es '{}'. No requiere envío de notificación.", estado);
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
