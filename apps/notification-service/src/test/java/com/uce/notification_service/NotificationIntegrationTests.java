package com.uce.notification_service;

import com.uce.notification_service.config.MqttClientManager;
import com.uce.notification_service.models.Notificacion;
import com.uce.notification_service.models.TipoNotificacion;
import com.uce.notification_service.repositories.NotificationRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NotificationIntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private MqttClientManager mqttClientManager;

    private final RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL test container configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");

        // Kafka test container configuration
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.group-id", () -> "notification-service-test-group");
    }

    @Test
    void testNotificationFlowAndEndpoints() {
        String studentId = "student_test_42";
        String baseUrl = "http://localhost:" + port;

        // 1. Verify health endpoint
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(baseUrl + "/health", String.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        assertEquals("notification-service is running", healthResponse.getBody());

        // 2. Publish a VALIDADO event to Kafka topic
        String eventJson = "{"
                + "\"id\":101,"
                + "\"estudianteId\":\"" + studentId + "\","
                + "\"proyectoId\":\"Proyecto Vinculacion UCE\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":5.0,"
                + "\"descripcionActividad\":\"Desarrollo de modulos\","
                + "\"estado\":\"VALIDADO\","
                + "\"tutorId\":\"tutor_10\","
                + "\"fechaValidacion\":\"2026-06-16T10:00:00\""
                + "}";

        kafkaTemplate.send("horas.registradas", studentId, eventJson);

        // 3. Wait and verify that consumer stores notification in DB and publishes to MQTT
        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    List<Notificacion> list = repository.findByEstudianteIdOrderByCreatedAtDesc(studentId);
                    assertFalse(list.isEmpty());
                    
                    Notificacion notif = list.get(0);
                    assertEquals(studentId, notif.getEstudianteId());
                    assertEquals(101L, notif.getHorasId());
                    assertEquals(TipoNotificacion.HORAS_VALIDADAS, notif.getTipo());
                    assertEquals("Se han validado sus horas para el proyecto Proyecto Vinculacion UCE el 2026-06-16T10:00:00 por el tutor tutor_10.", notif.getMensaje());
                    assertFalse(notif.getLeida());

                    // Verify MQTT publish was called on dynamic topic: notificaciones/student_test_42
                    Mockito.verify(mqttClientManager, Mockito.atLeastOnce())
                            .publish(eq("notificaciones/" + studentId), anyString());
                });

        // 4. Retrieve notifications via GET REST API
        ResponseEntity<Notificacion[]> getResponse = restTemplate.getForEntity(
                baseUrl + "/api/notifications/student/" + studentId,
                Notificacion[].class
        );
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        Notificacion[] notifications = getResponse.getBody();
        assertNotNull(notifications);
        assertEquals(1, notifications.length);
        assertEquals(studentId, notifications[0].getEstudianteId());

        Long notifId = notifications[0].getId();

        // 5. Mark notification as read via PATCH API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> patchEntity = new HttpEntity<>("{}", headers);

        ResponseEntity<Notificacion> patchResponse = restTemplate.exchange(
                baseUrl + "/api/notifications/" + notifId + "/read",
                HttpMethod.PATCH,
                patchEntity,
                Notificacion.class
        );
        assertEquals(HttpStatus.OK, patchResponse.getStatusCode());
        Notificacion readNotif = patchResponse.getBody();
        assertNotNull(readNotif);
        assertTrue(readNotif.getLeida());

        // Verify the database has updated
        Notificacion dbNotif = repository.findById(notifId).orElseThrow();
        assertTrue(dbNotif.getLeida());

        // 6. Test 404 for non-existent notification PATCH
        try {
            restTemplate.exchange(
                    baseUrl + "/api/notifications/99999/read",
                    HttpMethod.PATCH,
                    patchEntity,
                    Notificacion.class
            );
            fail("Debería lanzar HttpClientErrorException.NotFound");
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
        }
    }
}
