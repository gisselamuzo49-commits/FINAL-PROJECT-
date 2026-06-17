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
        // Desactivamos auto-configuración de Kafka para que no intente conectarse al broker real
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    }
)
class NotificationServiceApplicationTests {

    @MockitoBean
    private MqttClientManager mqttClientManager;

    @MockitoBean
    @SuppressWarnings("rawtypes")
    private KafkaTemplate kafkaTemplate;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring cargue correctamente
    }
}
