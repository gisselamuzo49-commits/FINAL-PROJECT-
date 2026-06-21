package com.uce.report_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.report_service.models.RegistroHorasReporte;
import com.uce.report_service.repositories.RegistroHorasReporteRepository;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import com.uce.report_service.repositories.ReporteGlobalRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KafkaReportConsumerCircuitBreakerTest {

    private RegistroHorasReporteRepository registroRepository;
    private ReporteEstudianteRepository studentRepository;
    private ReporteGlobalRepository globalRepository;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private KafkaReportConsumer consumer;

    @BeforeEach
    void setUp() {
        registroRepository = mock(RegistroHorasReporteRepository.class);
        studentRepository = mock(ReporteEstudianteRepository.class);
        globalRepository = mock(ReporteGlobalRepository.class);
        restTemplate = mock(RestTemplate.class);
        objectMapper = new ObjectMapper();

        // Configure a fast Circuit Breaker for test purposes to avoid slow Thread.sleep
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofMillis(100)) // 100ms wait
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry registry = io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of(config);
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = registry.circuitBreaker("documentService");

        consumer = new KafkaReportConsumer();
        consumer.setCircuitBreaker(cb);
        consumer.setRegistroRepository(registroRepository);
        consumer.setStudentRepository(studentRepository);
        consumer.setGlobalRepository(globalRepository);
        consumer.setRestTemplate(restTemplate);
        consumer.setObjectMapper(objectMapper);
        consumer.setDocumentServiceUrl("http://localhost:8088");
    }

    @Test
    void whenRestTemplateSucceeds_stateIsClosed() {
        String message = "{\"id\":\"reg-1\",\"estudianteId\":\"student-1\",\"horas\":10.5,\"estado\":\"VALIDADO\"}";
        
        RegistroHorasReporte r1 = new RegistroHorasReporte("reg-1", "student-1", new BigDecimal("10.5"), "VALIDADO");
        when(registroRepository.findByEstudianteId("student-1")).thenReturn(Collections.singletonList(r1));

        Map<String, Object> mockDocResponse = new HashMap<>();
        mockDocResponse.put("totalDocumentos", 2);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockDocResponse);

        when(studentRepository.findByEstudianteId("student-1")).thenReturn(Optional.empty());

        consumer.consume(message);

        assertEquals("CLOSED", consumer.getCircuitBreakerState());
        verify(restTemplate, times(1)).getForObject(anyString(), eq(Map.class));
    }

    @Test
    void whenRestTemplateFailsRepeatedly_circuitBreakerOpensAndShortCircuits() throws InterruptedException {
        String message = "{\"id\":\"reg-1\",\"estudianteId\":\"student-1\",\"horas\":10.5,\"estado\":\"VALIDADO\"}";
        
        RegistroHorasReporte r1 = new RegistroHorasReporte("reg-1", "student-1", new BigDecimal("10.5"), "VALIDADO");
        when(registroRepository.findByEstudianteId("student-1")).thenReturn(Collections.singletonList(r1));
        when(studentRepository.findByEstudianteId("student-1")).thenReturn(Optional.empty());

        // Setup rest template to fail
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Service Unavailable"));

        // Circuit breaker is CLOSED initially
        assertEquals("CLOSED", consumer.getCircuitBreakerState());

        // Make first failed call
        consumer.consume(message);
        assertEquals("CLOSED", consumer.getCircuitBreakerState());

        // Make second failed call
        consumer.consume(message);
        
        // Circuit breaker should now be OPEN because minimumNumberOfCalls (2) met and 100% failure rate
        assertEquals("OPEN", consumer.getCircuitBreakerState());

        // The third call should be short-circuited (restTemplate not called again)
        reset(restTemplate);
        consumer.consume(message);
        verifyNoInteractions(restTemplate);

        // Wait 150ms for waitDurationInOpenState (100ms) to elapse
        Thread.sleep(150);

        // Next call should transition state to HALF_OPEN and invoke restTemplate
        Map<String, Object> mockDocResponse = new HashMap<>();
        mockDocResponse.put("totalDocumentos", 3);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockDocResponse);

        consumer.consume(message);
        assertEquals("HALF_OPEN", consumer.getCircuitBreakerState());

        // In HALF_OPEN state, we configured permittedNumberOfCallsInHalfOpenState = 2.
        // Let's make one more successful call to close the circuit breaker
        consumer.consume(message);

        // Now Circuit Breaker should be CLOSED again
        assertEquals("CLOSED", consumer.getCircuitBreakerState());
    }
}
