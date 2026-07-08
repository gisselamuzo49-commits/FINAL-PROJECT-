package com.uce.hours_service.outbox;

import com.uce.hours_service.models.OutboxEvent;
import com.uce.hours_service.repositories.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final String TOPIC = "horas.registradas";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutboxEventRepository outboxRepository;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish to Kafka", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Envía el mensaje con la clave aggregateId (estudianteId) y el payload (JSON)
                var future = kafkaTemplate.send(TOPIC, event.getAggregateId(), event.getPayload());
                if (future != null) {
                    future.get(); // .get() bloquea para asegurar éxito
                }
                
                event.setStatus("SENT");
                outboxRepository.save(event);
                log.info("Successfully published outbox event ID {} to topic {}", event.getId(), TOPIC);
            } catch (Exception e) {
                log.error("Failed to publish outbox event ID {} to topic {}. Will retry in the next cycle.", 
                        event.getId(), TOPIC, e);
                // Si falla, el estado se mantiene como PENDING en la base de datos para reintento.
            }
        }
    }
}
