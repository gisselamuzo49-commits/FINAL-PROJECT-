package com.uce.notification_service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uce.notification_service.config.MqttClientManager;
import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private MqttClientManager mqttClientManager;

    private final ObjectMapper objectMapper;

    public NotificationService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Para manejar LocalDateTime
    }

    @Transactional
    public Notificacion createNotification(Notificacion notificacion) {
        logger.info("Guardando nueva notificación en base de datos para estudiante: {}", notificacion.getEstudianteId());
        Notificacion saved = repository.save(notificacion);

        // Publicar a MQTT
        try {
            String topic = "notificaciones/" + saved.getEstudianteId();
            String payload = objectMapper.writeValueAsString(saved);
            mqttClientManager.publish(topic, payload);
        } catch (Exception e) {
            logger.error("Error al serializar notificación para publicación MQTT: {}", e.getMessage());
        }

        return saved;
    }

    public List<Notificacion> getNotificationsByStudent(String estudianteId) {
        logger.info("Obteniendo notificaciones para estudiante: {}", estudianteId);
        return repository.findByEstudianteIdOrderByCreatedAtDesc(estudianteId);
    }

    @Transactional
    public Optional<Notificacion> markAsRead(Long id) {
        logger.info("Marcando notificación con ID: {} como leída", id);
        return repository.findById(id).map(notificacion -> {
            notificacion.setLeida(true);
            Notificacion updated = repository.save(notificacion);
            
            // Publicar actualización a MQTT en el topic del estudiante
            try {
                String topic = "notificaciones/" + updated.getEstudianteId();
                String payload = objectMapper.writeValueAsString(updated);
                mqttClientManager.publish(topic, payload);
            } catch (Exception e) {
                logger.error("Error al serializar actualización de notificación para MQTT: {}", e.getMessage());
            }
            
            return updated;
        });
    }
}
