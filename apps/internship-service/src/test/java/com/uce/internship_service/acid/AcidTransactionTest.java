package com.uce.internship_service.acid;

import com.uce.internship_service.graph.EstudianteNode;
import com.uce.internship_service.graph.OfertaNode;
import com.uce.internship_service.models.Internship;
import com.uce.internship_service.repositories.EstudianteNodeRepository;
import com.uce.internship_service.repositories.InternshipRepository;
import com.uce.internship_service.repositories.OfertaNodeRepository;
import com.uce.internship_service.repositories.StudentApplicationDto;
import com.uce.internship_service.services.PostulacionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration,org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "elasticsearch.url=http://localhost:9200",
    "elasticsearch.index=ofertas_test"
})
public class AcidTransactionTest {

    @Autowired
    private PostulacionService postulacionService;

    @MockitoBean
    private InternshipRepository internshipRepository;

    @MockitoBean
    private EstudianteNodeRepository estudianteNodeRepository;

    @MockitoBean
    private OfertaNodeRepository ofertaNodeRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    private com.uce.internship_service.elasticsearch.OfertaSearchService ofertaSearchService;

    @Test
    public void testCreateApplication_Success_AllThreeStepsExecute() {
        Internship internship = new Internship();
        internship.setId(1L);
        internship.setTitle("Pasantía IA");
        internship.setCompany("UCE");

        when(internshipRepository.findById(1L)).thenReturn(Optional.of(internship));
        when(estudianteNodeRepository.existsApplication(anyString(), anyString())).thenReturn(false);
        when(ofertaNodeRepository.findById(anyString())).thenReturn(Optional.of(new OfertaNode("1", "Pasantía IA", "UCE")));
        when(estudianteNodeRepository.findById(anyString())).thenReturn(Optional.of(new EstudianteNode("est-123")));
        
        StudentApplicationDto appDto = mock(StudentApplicationDto.class);
        when(appDto.internshipId()).thenReturn("1");
        when(estudianteNodeRepository.findApplicationsByEstudianteId("est-123")).thenReturn(List.of(appDto));

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(new CompletableFuture<>());

        StudentApplicationDto result = postulacionService.createApplication(1L, "est-123", "Me interesa");

        assertNotNull(result);
        verify(estudianteNodeRepository, times(1)).save(any(EstudianteNode.class));
        verify(kafkaTemplate, times(1)).send(eq("postulacion.creada"), eq("est-123"), anyString());
    }

    @Test
    public void testCreateApplication_KafkaFails_ThrowsExceptionAndRollback() {
        Internship internship = new Internship();
        internship.setId(1L);
        internship.setTitle("Pasantía IA");
        internship.setCompany("UCE");

        when(internshipRepository.findById(1L)).thenReturn(Optional.of(internship));
        when(estudianteNodeRepository.existsApplication(anyString(), anyString())).thenReturn(false);
        when(ofertaNodeRepository.findById(anyString())).thenReturn(Optional.of(new OfertaNode("1", "Pasantía IA", "UCE")));
        when(estudianteNodeRepository.findById(anyString())).thenReturn(Optional.of(new EstudianteNode("est-123")));

        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka Broker Down"));

        assertThrows(RuntimeException.class, () -> {
            postulacionService.createApplication(1L, "est-123", "Me interesa");
        });
    }
}
