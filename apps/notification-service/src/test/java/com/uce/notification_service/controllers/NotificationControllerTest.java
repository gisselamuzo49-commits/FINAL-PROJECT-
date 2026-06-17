package com.uce.notification_service.controllers;

import com.uce.notification_service.NotificationServiceApplication;
import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.models.TipoNotificacion;
import com.uce.notification_service.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private final NotificationServiceApplication notificationServiceApplication = new NotificationServiceApplication();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(notificationController, notificationServiceApplication).build();
    }

    @Test
    void testGetNotificationsWithData() throws Exception {
        List<Notificacion> notifications = new ArrayList<>();
        Notificacion notif = new Notificacion("123", "Se han validado sus horas", TipoNotificacion.HORAS_VALIDADAS, 10L);
        notif.setId(1L);
        notifications.add(notif);

        Mockito.when(notificationService.getNotificationsByStudent("123"))
                .thenReturn(notifications);

        mockMvc.perform(get("/api/notifications/student/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].estudianteId").value("123"))
                .andExpect(jsonPath("$[0].mensaje").value("Se han validado sus horas"))
                .andExpect(jsonPath("$[0].tipo").value("HORAS_VALIDADAS"));
    }

    @Test
    void testGetNotificationsWithoutData() throws Exception {
        Mockito.when(notificationService.getNotificationsByStudent("123"))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/notifications/student/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void testMarkAsReadSuccessful() throws Exception {
        Notificacion notif = new Notificacion("123", "Se han validado sus horas", TipoNotificacion.HORAS_VALIDADAS, 10L);
        notif.setId(1L);
        notif.setLeida(true);

        Mockito.when(notificationService.markAsRead(eq(1L)))
                .thenReturn(Optional.of(notif));

        mockMvc.perform(patch("/api/notifications/1/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.leida").value(true));
    }

    @Test
    void testMarkAsReadNotFound() throws Exception {
        Mockito.when(notificationService.markAsRead(eq(999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/notifications/999/read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("notification-service is running"));
    }
}
