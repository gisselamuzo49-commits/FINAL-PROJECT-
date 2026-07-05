package com.uce.notification_service;

import com.uce.notification_service.consumers.KafkaNotificationConsumer;
import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.repositories.NotificationRepository;
import com.uce.notification_service.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
    classes = NotificationServiceApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,org.springframework.boot.data.cassandra.autoconfigure.DataCassandraAutoConfiguration,org.springframework.boot.data.cassandra.autoconfigure.CassandraRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration,org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration"
    }
)
public class NotificationWebSocketTest {

    @Autowired
    private KafkaNotificationConsumer consumer;

    @Autowired
    private NotificationService notificationService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    @MockitoBean
    private com.uce.notification_service.config.MqttClientManager mqttClientManager;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private com.uce.notification_service.cassandra.CassandraEventService cassandraEventService;

    @MockitoBean
    private com.uce.notification_service.cassandra.NotificationEventRepository notificationEventRepository;

    @Test
    public void testKafkaEventTriggersWebSocketSend() {
        String eventJson = "{"
                + "\"id\":501,"
                + "\"estudianteId\":\"student_99\","
                + "\"proyectoId\":\"Proyecto Vinculacion\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":20.0,"
                + "\"descripcionActividad\":\"Actividad en UCE\","
                + "\"estado\":\"VALIDADO\","
                + "\"tutorId\":\"tutor_admin\","
                + "\"fechaValidacion\":\"2026-06-16T14:30:00\""
                + "}";

        consumer.consume(eventJson);

        ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Notificacion> payloadCaptor = ArgumentCaptor.forClass(Notificacion.class);

        verify(messagingTemplate, times(1)).convertAndSendToUser(
                userCaptor.capture(),
                destinationCaptor.capture(),
                payloadCaptor.capture()
        );

        assertEquals("student_99", userCaptor.getValue());
        assertEquals("/queue/notifications", destinationCaptor.getValue());
        assertNotNull(payloadCaptor.getValue());
        assertEquals("student_99", payloadCaptor.getValue().getEstudianteId());
    }
}
