package com.uce.internship_service;

import com.uce.internship_service.controllers.ApplicationRequest;
import com.uce.internship_service.controllers.StatusUpdateRequest;
import com.uce.internship_service.models.Internship;
import com.uce.internship_service.repositories.InternshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class PostulacionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.12.0-community")
            .withAdminPassword("password123");

    @LocalServerPort
    private int port;

    @Autowired
    private InternshipRepository internshipRepository;

    private final RestTemplate restTemplate = new RestTemplate(new org.springframework.http.client.JdkClientHttpRequestFactory());

    private String baseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");

        // Neo4j
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/internships";
        internshipRepository.deleteAll();
    }

    @Test
    void testEndToEndPostulacionesFlow() {
        // 1. Crear una oferta en PostgreSQL
        Internship internship = new Internship("Desarrollador Java", "Google", "Interesante oferta", "ABIERTA");
        internship = internshipRepository.save(internship);
        Long internshipId = internship.getId();

        // 2. Intentar postular a una oferta que no existe (Debe dar 404)
        ApplicationRequest badRequest = new ApplicationRequest("student-1", "Me gustaría postular");
        try {
            restTemplate.postForEntity(baseUrl + "/999999/applications", badRequest, Map.class);
            fail("Debería lanzar HttpClientErrorException.NotFound");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        // 3. Postular correctamente
        ApplicationRequest req1 = new ApplicationRequest("student-1", "Mi postulación");
        ResponseEntity<Map> response1 = restTemplate.postForEntity(baseUrl + "/" + internshipId + "/applications", req1, Map.class);
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        Map app1 = response1.getBody();
        assertNotNull(app1);
        assertEquals("PENDIENTE", app1.get("estado"));
        assertEquals("Mi postulación", app1.get("mensaje"));
        assertEquals(String.valueOf(internshipId), app1.get("internshipId"));
        assertNotNull(app1.get("postulacionId"));
        Long postulacionId = ((Number) app1.get("postulacionId")).longValue();

        // 4. Intentar postular duplicado (Debe dar 400)
        try {
            restTemplate.postForEntity(baseUrl + "/" + internshipId + "/applications", req1, Map.class);
            fail("Debería lanzar HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }

        // 5. Obtener los aplicantes para la oferta (GET /api/internships/{internshipId}/applications)
        ResponseEntity<List<Map>> getApplicantsRes = restTemplate.exchange(
                baseUrl + "/" + internshipId + "/applications",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map>>() {}
        );
        assertEquals(HttpStatus.OK, getApplicantsRes.getStatusCode());
        List<Map> applicants = getApplicantsRes.getBody();
        assertNotNull(applicants);
        assertEquals(1, applicants.size());
        assertEquals("student-1", applicants.get(0).get("estudianteId"));
        assertEquals("PENDIENTE", applicants.get(0).get("estado"));
        assertEquals(postulacionId, ((Number) applicants.get(0).get("postulacionId")).longValue());

        // 6. Obtener las postulaciones de un estudiante (GET /api/internships/applications/student/{estudianteId})
        ResponseEntity<List<Map>> getStudentAppsRes = restTemplate.exchange(
                baseUrl + "/applications/student/student-1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map>>() {}
        );
        assertEquals(HttpStatus.OK, getStudentAppsRes.getStatusCode());
        List<Map> studentApps = getStudentAppsRes.getBody();
        assertNotNull(studentApps);
        assertEquals(1, studentApps.size());
        assertEquals(String.valueOf(internshipId), studentApps.get(0).get("internshipId"));
        assertEquals("Desarrollador Java", studentApps.get(0).get("title"));

        // 7. Modificar estado (PATCH /api/internships/applications/{postulacionId}/status)
        StatusUpdateRequest patchReq = new StatusUpdateRequest("ACEPTADA");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-User", "tutor-1");
        headers.set("X-Auth-Roles", "TUTOR");
        ResponseEntity<Map> patchRes = restTemplate.exchange(
                baseUrl + "/applications/" + postulacionId + "/status",
                HttpMethod.PATCH,
                new HttpEntity<>(patchReq, headers),
                Map.class
        );
        assertEquals(HttpStatus.OK, patchRes.getStatusCode());
        Map updatedApp = patchRes.getBody();
        assertNotNull(updatedApp);
        assertEquals("ACEPTADA", updatedApp.get("estado"));

        // 8. Intentar modificar con un estado inválido (Debe dar 400)
        try {
            StatusUpdateRequest badPatchReq = new StatusUpdateRequest("INVAL_STATE");
            restTemplate.exchange(
                    baseUrl + "/applications/" + postulacionId + "/status",
                    HttpMethod.PATCH,
                    new HttpEntity<>(badPatchReq, headers),
                    Map.class
            );
            fail("Debería lanzar HttpClientErrorException.BadRequest");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
    }
}
