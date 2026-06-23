package com.uce.notification_service.repositories;

import com.uce.notification_service.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByEstudianteIdOrderByCreatedAtDesc(String estudianteId);
}
