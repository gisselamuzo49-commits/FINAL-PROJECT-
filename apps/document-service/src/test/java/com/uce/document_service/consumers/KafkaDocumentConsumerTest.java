package com.uce.document_service.consumers;

import com.uce.document_service.DocumentServiceApplication;
import com.uce.document_service.models.DocumentoGenerado;
import com.uce.document_service.models.DocumentoResumen;
import com.uce.document_service.models.EstadoDocumento;
import com.uce.document_service.models.TipoDocumento;
import com.uce.document_service.repositories.DocumentoGeneradoRepository;
import com.uce.document_service.repositories.DocumentoResumenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(
    classes = DocumentServiceApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb_consumer;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "s3.bucket.name=pasantias-documents-test",
        // Desactivamos la auto-configuración de Kafka y Mongo para el test local
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration," +
            "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
    }
)
class KafkaDocumentConsumerTest {

    @Autowired
    private KafkaDocumentConsumer consumer;

    @Autowired
    private DocumentoGeneradoRepository postgresRepository;

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private DocumentoResumenRepository mongoRepository;

    @BeforeEach
    void setup() {
        postgresRepository.deleteAll();
        Mockito.reset(s3Client, mongoRepository);
        
        // Mock S3Client putObject
        Mockito.when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        // Mock MongoDB findById
        Mockito.when(mongoRepository.findById(any(String.class)))
                .thenReturn(Optional.empty());
    }

    @Test
    void testConsumeValidado() {
        String eventJson = "{"
                + "\"id\":501,"
                + "\"estudianteId\":\"student_99\","
                + "\"proyectoId\":\"proj-z\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":8.0,"
                + "\"descripcionActividad\":\"Testing\","
                + "\"estado\":\"VALIDADO\","
                + "\"tutorId\":\"tutor_1\","
                + "\"fechaValidacion\":\"2026-06-16T12:00:00\""
                + "}";

        consumer.consume(eventJson);

        // 1. Verify S3Client upload was invoked
        Mockito.verify(s3Client, Mockito.times(1))
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // 2. Verify PostgreSQL metadata storage
        List<DocumentoGenerado> list = postgresRepository.findByEstudianteIdOrderByCreatedAtDesc("student_99");
        assertEquals(1, list.size());
        DocumentoGenerado doc = list.get(0);
        assertEquals("student_99", doc.getEstudianteId());
        assertEquals("proj-z", doc.getProyectoId());
        assertEquals(TipoDocumento.CERTIFICADO_HORAS, doc.getTipo());
        assertEquals(EstadoDocumento.GENERADO, doc.getEstado());
        assertEquals("https://pasantias-documents-test.s3.amazonaws.com/documents/student_99_501.pdf", doc.getS3Url());

        // 3. Verify MongoDB upsert was invoked
        Mockito.verify(mongoRepository, Mockito.times(1))
                .save(any(DocumentoResumen.class));
    }

    @Test
    void testConsumePendiente() {
        String eventJson = "{"
                + "\"id\":502,"
                + "\"estudianteId\":\"student_99\","
                + "\"proyectoId\":\"proj-z\","
                + "\"fecha\":\"2026-06-15\","
                + "\"horas\":8.0,"
                + "\"descripcionActividad\":\"Testing\","
                + "\"estado\":\"PENDIENTE\","
                + "\"tutorId\":null,"
                + "\"fechaValidacion\":null"
                + "}";

        consumer.consume(eventJson);

        // Verify S3Client upload was NEVER invoked
        Mockito.verify(s3Client, Mockito.never())
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // Verify PostgreSQL has NO records saved
        List<DocumentoGenerado> list = postgresRepository.findByEstudianteIdOrderByCreatedAtDesc("student_99");
        assertTrue(list.isEmpty());

        // Verify MongoDB upsert was NEVER invoked
        Mockito.verify(mongoRepository, Mockito.never())
                .save(any(DocumentoResumen.class));
    }
}
