package com.uce.report_service.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.report_service.models.RegistroHorasReporte;
import com.uce.report_service.models.ReporteEstudiante;
import com.uce.report_service.models.ReporteGlobal;
import com.uce.report_service.repositories.RegistroHorasReporteRepository;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import com.uce.report_service.repositories.ReporteGlobalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaReportConsumerTest {

    @Mock
    private RegistroHorasReporteRepository registroRepository;

    @Mock
    private ReporteEstudianteRepository studentRepository;

    @Mock
    private ReporteGlobalRepository globalRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private KafkaReportConsumer consumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consumer, "documentServiceUrl", "http://localhost:8088");
    }

    @Test
    void testConsumeValidado() throws Exception {
        // Arrange
        String message = "{\"id\":\"reg-1\",\"estudianteId\":\"student-1\",\"horas\":10.5,\"estado\":\"VALIDADO\"}";
        
        RegistroHorasReporte r1 = new RegistroHorasReporte("reg-1", "student-1", new BigDecimal("10.5"), "VALIDADO");
        when(registroRepository.findByEstudianteId("student-1")).thenReturn(Collections.singletonList(r1));

        Map<String, Object> mockDocResponse = new HashMap<>();
        mockDocResponse.put("totalDocumentos", 2);
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(mockDocResponse);

        when(studentRepository.findByEstudianteId("student-1")).thenReturn(Optional.empty());
        
        ReporteEstudiante mockStudentReport = new ReporteEstudiante("student-1", new BigDecimal("10.5"), BigDecimal.ZERO, 2, java.time.LocalDateTime.now());
        when(studentRepository.findAll()).thenReturn(Collections.singletonList(mockStudentReport));

        // Act
        consumer.consume(message);

        // Assert
        verify(registroRepository).save(any(RegistroHorasReporte.class));
        
        ArgumentCaptor<ReporteEstudiante> studentCaptor = ArgumentCaptor.forClass(ReporteEstudiante.class);
        verify(studentRepository).save(studentCaptor.capture());
        ReporteEstudiante savedStudentReport = studentCaptor.getValue();
        assertEquals("student-1", savedStudentReport.getEstudianteId());
        assertEquals(new BigDecimal("10.5"), savedStudentReport.getTotalHorasValidadas());
        assertEquals(BigDecimal.ZERO, savedStudentReport.getTotalHorasPendientes());
        assertEquals(2, savedStudentReport.getTotalDocumentos());

        ArgumentCaptor<ReporteGlobal> globalCaptor = ArgumentCaptor.forClass(ReporteGlobal.class);
        verify(globalRepository).save(globalCaptor.capture());
        ReporteGlobal savedGlobalReport = globalCaptor.getValue();
        assertEquals("global", savedGlobalReport.getId());
        assertEquals(1, savedGlobalReport.getTotalEstudiantes());
        assertEquals(new BigDecimal("10.5"), savedGlobalReport.getTotalHorasValidadas());
        assertEquals(BigDecimal.ZERO, savedGlobalReport.getTotalHorasPendientes());
    }

    @Test
    void testConsumePendiente() throws Exception {
        // Arrange
        String message = "{\"id\":\"reg-2\",\"estudianteId\":\"student-2\",\"horas\":5.0,\"estado\":\"PENDIENTE\"}";
        
        RegistroHorasReporte r2 = new RegistroHorasReporte("reg-2", "student-2", new BigDecimal("5.0"), "PENDIENTE");
        when(registroRepository.findByEstudianteId("student-2")).thenReturn(Collections.singletonList(r2));

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenThrow(new RuntimeException("Service Down"));

        when(studentRepository.findByEstudianteId("student-2")).thenReturn(Optional.empty());

        ReporteEstudiante mockStudentReport = new ReporteEstudiante("student-2", BigDecimal.ZERO, new BigDecimal("5.0"), 0, java.time.LocalDateTime.now());
        when(studentRepository.findAll()).thenReturn(Collections.singletonList(mockStudentReport));

        // Act
        consumer.consume(message);

        // Assert
        verify(registroRepository).save(any(RegistroHorasReporte.class));
        
        ArgumentCaptor<ReporteEstudiante> studentCaptor = ArgumentCaptor.forClass(ReporteEstudiante.class);
        verify(studentRepository).save(studentCaptor.capture());
        ReporteEstudiante savedStudentReport = studentCaptor.getValue();
        assertEquals("student-2", savedStudentReport.getEstudianteId());
        assertEquals(BigDecimal.ZERO, savedStudentReport.getTotalHorasValidadas());
        assertEquals(new BigDecimal("5.0"), savedStudentReport.getTotalHorasPendientes());
        assertEquals(0, savedStudentReport.getTotalDocumentos());

        ArgumentCaptor<ReporteGlobal> globalCaptor = ArgumentCaptor.forClass(ReporteGlobal.class);
        verify(globalRepository).save(globalCaptor.capture());
        ReporteGlobal savedGlobalReport = globalCaptor.getValue();
        assertEquals(1, savedGlobalReport.getTotalEstudiantes());
        assertEquals(BigDecimal.ZERO, savedGlobalReport.getTotalHorasValidadas());
        assertEquals(new BigDecimal("5.0"), savedGlobalReport.getTotalHorasPendientes());
    }

    @Test
    void testRecalculateTotals() {
        // Arrange
        ReporteEstudiante r1 = new ReporteEstudiante("st-1", new BigDecimal("15.0"), new BigDecimal("5.0"), 1, java.time.LocalDateTime.now());
        ReporteEstudiante r2 = new ReporteEstudiante("st-2", new BigDecimal("20.0"), new BigDecimal("10.0"), 3, java.time.LocalDateTime.now());
        when(studentRepository.findAll()).thenReturn(Arrays.asList(r1, r2));

        // Act
        consumer.recalculateGlobalReport();

        // Assert
        ArgumentCaptor<ReporteGlobal> globalCaptor = ArgumentCaptor.forClass(ReporteGlobal.class);
        verify(globalRepository).save(globalCaptor.capture());
        ReporteGlobal savedGlobalReport = globalCaptor.getValue();
        assertEquals(2, savedGlobalReport.getTotalEstudiantes());
        assertEquals(new BigDecimal("35.0"), savedGlobalReport.getTotalHorasValidadas());
        assertEquals(new BigDecimal("15.0"), savedGlobalReport.getTotalHorasPendientes());
        assertEquals(2, savedGlobalReport.getEstudiantesPorFacultad().get("FICA"));
    }
}
