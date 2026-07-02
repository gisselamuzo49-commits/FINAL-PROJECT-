package com.uce.hours_service;

import com.uce.hours_service.models.HorasResumen;
import com.uce.hours_service.models.RegistroHoras;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test using real containers (PostgreSQL, Kafka, MongoDB).
 * Exercises the full CQRS pipeline: HTTP POST → Postgres → Kafka → Consumer → MongoDB.
 * gRPC client points to a non-existent service, verifying the "best effort" fallback.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class HoursServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL (overrides test application.properties H2 defaults)
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");

        // Kafka (broker address + producer + consumer config)
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.key-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer",
                () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.acks", () -> "1");
        registry.add("spring.kafka.consumer.group-id", () -> "hours-service-group");
        registry.add("spring.kafka.consumer.key-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer",
                () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");

        // MongoDB
        registry.add("spring.mongodb.uri", mongo::getReplicaSetUrl);

        // gRPC → points to nothing (exercises best-effort fallback path)
        registry.add("grpc.client.user-service.address", () -> "static://localhost:9083");
        registry.add("grpc.client.user-service.negotiationType", () -> "PLAINTEXT");
    }

    @Test
    void testEndToEndCQRSFlow() {
        String studentId = "est-integration-1";
        String baseUrl = "http://localhost:" + port;

        // ── Step 1: POST /api/hours → creates in PostgreSQL, publishes Kafka event ──
        RegistroHoras newRecord = new RegistroHoras();
        newRecord.setEstudianteId(studentId);
        newRecord.setProyectoId("proj-1");
        newRecord.setFecha(LocalDate.of(2026, 6, 15));
        newRecord.setHoras(5.0);
        newRecord.setDescripcionActividad("E2E Test Activity");

        ResponseEntity<RegistroHoras> postResponse = restTemplate.postForEntity(
                baseUrl + "/api/hours",
                newRecord,
                RegistroHoras.class
        );

        assertEquals(HttpStatus.OK, postResponse.getStatusCode());
        RegistroHoras createdRecord = postResponse.getBody();
        assertNotNull(createdRecord);
        assertNotNull(createdRecord.getId());
        assertEquals(studentId, createdRecord.getEstudianteId());
        assertEquals("PENDIENTE", createdRecord.getEstado().toString());

        // ── Step 2: Await Kafka consumer → MongoDB projection created ──
        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    ResponseEntity<HorasResumen> getResponse = restTemplate.getForEntity(
                            baseUrl + "/api/hours/student/" + studentId,
                            HorasResumen.class
                    );
                    assertEquals(HttpStatus.OK, getResponse.getStatusCode());
                    HorasResumen resumen = getResponse.getBody();
                    assertNotNull(resumen);
                    assertEquals(studentId, resumen.getEstudianteId());
                    assertEquals(5.0, resumen.getTotalHorasPendientes());
                    assertEquals(0.0, resumen.getTotalHorasValidadas());
                    // gRPC is unavailable → nombre and carrera remain null (best-effort fallback)
                    assertNull(resumen.getNombre());
                    assertNull(resumen.getCarrera());
                    assertEquals(1, resumen.getHistorial().size());
                    assertEquals("proj-1", resumen.getHistorial().get(0).getProyectoId());
                });

        // ── Step 3: PATCH /api/hours/{id}/validar → validates in PostgreSQL ──
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-User", "tutor-1");
        headers.set("X-Auth-Roles", "TUTOR");
        HttpEntity<String> patchEntity = new HttpEntity<>(
                "{\"tutorId\":\"tutor-1\",\"aprobado\":true}", headers);

        ResponseEntity<RegistroHoras> patchResponse = restTemplate.exchange(
                baseUrl + "/api/hours/" + createdRecord.getId() + "/validar",
                HttpMethod.PATCH,
                patchEntity,
                RegistroHoras.class
        );

        assertEquals(HttpStatus.OK, patchResponse.getStatusCode());
        RegistroHoras validatedRecord = patchResponse.getBody();
        assertNotNull(validatedRecord);
        assertEquals("VALIDADO", validatedRecord.getEstado().toString());

        // ── Step 4: Await Kafka consumer → MongoDB projection updated ──
        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    ResponseEntity<HorasResumen> getResponse2 = restTemplate.getForEntity(
                            baseUrl + "/api/hours/student/" + studentId,
                            HorasResumen.class
                    );
                    assertEquals(HttpStatus.OK, getResponse2.getStatusCode());
                    HorasResumen resumen = getResponse2.getBody();
                    assertNotNull(resumen);
                    assertEquals(5.0, resumen.getTotalHorasValidadas());
                    assertEquals(0.0, resumen.getTotalHorasPendientes());
                });
    }
}

