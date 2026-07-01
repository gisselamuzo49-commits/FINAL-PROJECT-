package com.uce.notification_service;

import com.uce.notification_service.config.MqttClientManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(
    classes = NotificationServiceApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Desactivamos auto-configuración de Kafka y Cassandra para que no intente conectarse a brokers reales
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,org.springframework.boot.data.cassandra.autoconfigure.DataCassandraAutoConfiguration,org.springframework.boot.data.cassandra.autoconfigure.CassandraRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration,org.springframework.boot.autoconfigure.data.cassandra.CassandraRepositoriesAutoConfiguration"
    }
)
class NotificationServiceApplicationTests {

    @MockitoBean
    private MqttClientManager mqttClientManager;

    @MockitoBean
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @MockitoBean
    private com.uce.notification_service.cassandra.CassandraEventService cassandraEventService;

    @MockitoBean
    private com.uce.notification_service.cassandra.NotificationEventRepository notificationEventRepository;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring cargue correctamente
    }
}
