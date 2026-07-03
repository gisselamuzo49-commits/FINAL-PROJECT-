package com.uce.internship_service.services;

import com.uce.internship_service.models.Internship;
import com.uce.internship_service.repositories.InternshipRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration,org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "elasticsearch.url=http://localhost:9200",
    "elasticsearch.index=ofertas_test"
})
public class InternshipServiceCircuitBreakerTest {

    @Autowired
    private InternshipService internshipService;

    @MockitoBean
    private InternshipRepository internshipRepository;

    @MockitoBean
    private com.uce.internship_service.kafka.PostulacionEventProducer postulacionEventProducer;

    @MockitoBean
    private com.uce.internship_service.elasticsearch.OfertaSearchService ofertaSearchService;

    @Test
    public void testGetAllInternshipsFallback_WhenRepositoryThrowsException() {
        Mockito.when(internshipRepository.findAll())
                .thenThrow(new RuntimeException("DB Connection Timeout"));

        List<Internship> results = internshipService.getAllInternships();

        assertTrue(results.isEmpty());
    }
}
