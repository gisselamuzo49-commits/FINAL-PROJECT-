package com.uce.document_service.services;

import com.uce.document_service.models.DocumentoGenerado;
import com.uce.document_service.models.EstadoDocumento;
import com.uce.document_service.models.TipoDocumento;
import com.uce.document_service.repositories.DocumentoGeneradoRepository;
import com.uce.document_service.repositories.DocumentoResumenRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    private DocumentoGeneradoRepository postgresRepository;
    private DocumentoResumenRepository mongoRepository;
    private S3Client mockS3Client;
    private DocumentService service;

    @BeforeEach
    void setUp() {
        postgresRepository = mock(DocumentoGeneradoRepository.class);
        mongoRepository = mock(DocumentoResumenRepository.class);
        mockS3Client = mock(S3Client.class);

        // Configure a fast Circuit Breaker for test purposes to avoid slow Thread.sleep
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofMillis(100)) // 100ms wait
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        service = new DocumentService(config);
        service.setPostgresRepository(postgresRepository);
        service.setMongoRepository(mongoRepository);
        service.setS3Client(mockS3Client);
        service.setBucketName("test-bucket");

        // Mock repo saves to return a mock doc with ID
        when(postgresRepository.save(any(DocumentoGenerado.class))).thenAnswer(invocation -> {
            DocumentoGenerado doc = invocation.getArgument(0);
            doc.setId(101L);
            return doc;
        });
    }

    @Test
    void whenS3UploadSucceeds_returnsGeneratedAndStateIsClosed() {
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        DocumentoGenerado result = service.generateAndUploadDocument("student_1", "project_1", 10.0, "2026-06-21", 1L);

        assertNotNull(result);
        assertEquals(EstadoDocumento.GENERADO, result.getEstado());
        assertEquals("https://test-bucket.s3.amazonaws.com/documents/student_1_1.pdf", result.getS3Url());
        assertEquals("CLOSED", service.getCircuitBreakerState());
    }

    @Test
    void whenS3UploadFailsRepeatedly_circuitBreakerOpensAndShortCircuits() throws InterruptedException {
        // Setup mock to throw an S3Exception when uploading
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("S3 connection failed").build());

        // Circuit breaker is CLOSED initially
        assertEquals("CLOSED", service.getCircuitBreakerState());

        // Make first failed call
        DocumentoGenerado res1 = service.generateAndUploadDocument("student_1", "project_1", 10.0, "2026-06-21", 1L);
        assertEquals(EstadoDocumento.ERROR, res1.getEstado());

        // Make second failed call
        DocumentoGenerado res2 = service.generateAndUploadDocument("student_1", "project_1", 10.0, "2026-06-21", 2L);
        assertEquals(EstadoDocumento.ERROR, res2.getEstado());

        // Circuit breaker should now be OPEN because minimumNumberOfCalls (2) met and 100% failure rate
        assertEquals("OPEN", service.getCircuitBreakerState());

        // The third call should be short-circuited (mockS3Client.putObject not called again)
        reset(mockS3Client);
        DocumentoGenerado res3 = service.generateAndUploadDocument("student_1", "project_1", 10.0, "2026-06-21", 3L);
        assertEquals(EstadoDocumento.ERROR, res3.getEstado());
        verifyNoInteractions(mockS3Client);

        // Wait 150ms for waitDurationInOpenState (100ms) to elapse
        Thread.sleep(150);

        // Next call should transition state to HALF_OPEN and invoke mockS3Client
        when(mockS3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        DocumentoGenerado res4 = service.generateAndUploadDocument("student_1", "project_1", 10.0, "2026-06-21", 4L);
        assertEquals(EstadoDocumento.GENERADO, res4.getEstado());
        assertEquals("HALF_OPEN", service.getCircuitBreakerState());

        // In HALF_OPEN state, we configured permittedNumberOfCallsInHalfOpenState = 2.
        // Let's make one more successful call to close the circuit breaker
        DocumentoGenerado res5 = service.generateAndUploadDocument("student_1", "project_1", 10.0, "2026-06-21", 5L);
        assertEquals(EstadoDocumento.GENERADO, res5.getEstado());

        // Now Circuit Breaker should be CLOSED again
        assertEquals("CLOSED", service.getCircuitBreakerState());
    }
}
