package com.uce.notification_service.cassandra;

import org.springframework.stereotype.Service;

@Service
public class CassandraEventService {

    private final NotificationEventRepository repository;

    public CassandraEventService(NotificationEventRepository repository) {
        this.repository = repository;
    }

    public void logEvent(String estudianteId, String tipo, String mensaje) {
        NotificationEvent event = new NotificationEvent();
        event.setEstudianteId(estudianteId);
        event.setTipo(tipo);
        event.setMensaje(mensaje);
        event.setEstado("ENVIADO");
        repository.save(event);
    }

    public String getDatabaseInfo() {
        return "{\"database\": \"Cassandra\", " +
               "\"keyspace\": \"pasantias_events\", " +
               "\"table\": \"notification_events\", " +
               "\"status\": \"connected\"}";
    }
}
