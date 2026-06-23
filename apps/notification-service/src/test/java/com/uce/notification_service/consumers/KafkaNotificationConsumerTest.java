package com.uce.notification_service.consumers;

import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.models.TipoNotificacion;
import com.uce.notification_service.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private KafkaNotificationConsumer consumer;

    @Test
    void testEventoValidado() {
        String eventJson = "{"
                + "\"id\":201,"
                + "\"estudianteId\":\"student_42\","
                + "\"proyectoId\":\"Proyecto A\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":10.0,"
                + "\"descripcionActividad\":\"test\","
                + "\"estado\":\"VALIDADO\","
                + "\"tutorId\":\"tutor_x\","
                + "\"fechaValidacion\":\"2026-06-16T12:00:00\""
                + "}";

        consumer.consume(eventJson);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationService, times(1)).createNotification(captor.capture());
        
        Notificacion notif = captor.getValue();
        assertEquals("student_42", notif.getEstudianteId());
        assertEquals(201L, notif.getHorasId());
        assertEquals(TipoNotificacion.HORAS_VALIDADAS, notif.getTipo());
        assertTrue(notif.getMensaje().contains("2026-06-16T12:00:00"));
        assertTrue(notif.getMensaje().contains("tutor_x"));
    }

    @Test
    void testEventoRechazado() {
        String eventJson = "{"
                + "\"id\":202,"
                + "\"estudianteId\":\"student_42\","
                + "\"proyectoId\":\"Proyecto A\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":10.0,"
                + "\"descripcionActividad\":\"test\","
                + "\"estado\":\"RECHAZADO\","
                + "\"tutorId\":\"tutor_x\","
                + "\"fechaValidacion\":\"2026-06-16T12:00:00\""
                + "}";

        consumer.consume(eventJson);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationService, times(1)).createNotification(captor.capture());
        
        Notificacion notif = captor.getValue();
        assertEquals("student_42", notif.getEstudianteId());
        assertEquals(202L, notif.getHorasId());
        assertEquals(TipoNotificacion.HORAS_RECHAZADAS, notif.getTipo());
        assertTrue(notif.getMensaje().contains("Proyecto A"));
    }

    @Test
    void testEventoPendiente() {
        String eventJson = "{"
                + "\"id\":203,"
                + "\"estudianteId\":\"student_42\","
                + "\"proyectoId\":\"Proyecto A\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":10.0,"
                + "\"descripcionActividad\":\"test\","
                + "\"estado\":\"PENDIENTE\","
                + "\"tutorId\":null,"
                + "\"fechaValidacion\":null"
                + "}";

        consumer.consume(eventJson);

        verify(notificationService, never()).createNotification(any(Notificacion.class));
    }
}
