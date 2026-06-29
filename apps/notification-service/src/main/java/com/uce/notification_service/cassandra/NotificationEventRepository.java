package com.uce.notification_service.cassandra;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationEventRepository 
    extends CassandraRepository<NotificationEvent, String> {
    
    List<NotificationEvent> findByEstudianteId(String estudianteId);
}
