package com.uce.notification_service.controllers;

import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/student/{estudianteId}")
    public ResponseEntity<List<Notificacion>> getNotificationsByStudent(@PathVariable String estudianteId) {
        if (estudianteId == null || estudianteId.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Notificacion> notifications = notificationService.getNotificationsByStudent(estudianteId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notificacion> markAsRead(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Notificacion> updated = notificationService.markAsRead(id);
        return updated.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Autowired
    private com.uce.notification_service.cassandra.CassandraEventService cassandraEventService;

    @GetMapping("/cassandra/stats")
    public ResponseEntity<String> getCassandraStats() {
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(cassandraEventService.getDatabaseInfo());
    }
}
