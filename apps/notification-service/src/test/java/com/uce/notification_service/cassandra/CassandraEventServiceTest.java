package com.uce.notification_service.cassandra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CassandraEventServiceTest {

    private NotificationEventRepository repository;
    private CassandraEventService service;

    @BeforeEach
    void setUp() {
        repository = mock(NotificationEventRepository.class);
        service = new CassandraEventService(repository);
    }

    @Test
    void testLogEventCallsRepositorySave() {
        String estudianteId = "EST777";
        String tipo = "EMAIL";
        String mensaje = "Hola Mundo";

        when(repository.save(any(NotificationEvent.class))).thenReturn(new NotificationEvent());

        service.logEvent(estudianteId, tipo, mensaje);

        verify(repository, times(1)).save(any(NotificationEvent.class));
    }

    @Test
    void testGetDatabaseInfoReturnsCorrectJson() {
        String info = service.getDatabaseInfo();

        assertNotNull(info);
        assertTrue(info.contains("\"database\": \"Cassandra\""));
        assertTrue(info.contains("\"keyspace\": \"pasantias_events\""));
        assertTrue(info.contains("\"table\": \"notification_events\""));
        assertTrue(info.contains("\"status\": \"connected\""));
    }
}
