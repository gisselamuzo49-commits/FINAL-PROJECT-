package com.uce.internship_service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostulacionEventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC = "postulacion.creada";
    
    public PostulacionEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishPostulacionEvent(String estudianteId, String internshipId) {
        String event = String.format(
            "{\"estudianteId\":\"%s\",\"internshipId\":\"%s\",\"timestamp\":\"%s\"}",
            estudianteId, internshipId, java.time.Instant.now().toString()
        );
        kafkaTemplate.send(TOPIC, estudianteId, event);
    }
}
