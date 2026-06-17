package com.uce.report_service.ws;

import com.uce.report_service.models.ReporteEstudiante;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReporteEndpointTest {

    @Mock
    private ReporteEstudianteRepository repository;

    @InjectMocks
    private ReporteEndpoint endpoint;

    @Test
    void testGetReporteSuccess() {
        // Arrange
        GetReporteEstudianteRequest request = new GetReporteEstudianteRequest();
        request.setEstudianteId("student-123");

        ReporteEstudiante report = new ReporteEstudiante("student-123", new BigDecimal("30.0"), new BigDecimal("10.0"), 4, LocalDateTime.of(2026, 6, 17, 12, 0));
        when(repository.findByEstudianteId("student-123")).thenReturn(Optional.of(report));

        // Act
        GetReporteEstudianteResponse response = endpoint.getReporte(request);

        // Assert
        assertNotNull(response);
        assertEquals("student-123", response.getEstudianteId());
        assertEquals(new BigDecimal("30.0"), response.getTotalHorasValidadas());
        assertEquals(new BigDecimal("10.0"), response.getTotalHorasPendientes());
        assertEquals(4, response.getTotalDocumentos());
        assertNotNull(response.getUltimaActualizacion());
    }

    @Test
    void testGetReporteNotFound() {
        // Arrange
        GetReporteEstudianteRequest request = new GetReporteEstudianteRequest();
        request.setEstudianteId("student-non-existent");

        when(repository.findByEstudianteId("student-non-existent")).thenReturn(Optional.empty());

        // Act
        GetReporteEstudianteResponse response = endpoint.getReporte(request);

        // Assert
        assertNotNull(response);
        assertEquals("student-non-existent", response.getEstudianteId());
        assertEquals(BigDecimal.ZERO, response.getTotalHorasValidadas());
        assertEquals(BigDecimal.ZERO, response.getTotalHorasPendientes());
        assertEquals(0, response.getTotalDocumentos());
        assertNull(response.getUltimaActualizacion());
    }
}
