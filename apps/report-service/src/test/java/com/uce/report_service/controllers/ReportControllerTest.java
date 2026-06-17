package com.uce.report_service.controllers;

import com.uce.report_service.ReportServiceApplication;
import com.uce.report_service.models.ReporteEstudiante;
import com.uce.report_service.models.ReporteGlobal;
import com.uce.report_service.repositories.ReporteEstudianteRepository;
import com.uce.report_service.repositories.ReporteGlobalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReporteEstudianteRepository studentRepository;

    @Mock
    private ReporteGlobalRepository globalRepository;

    @InjectMocks
    private ReportController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testGetStudentReportSuccess() throws Exception {
        ReporteEstudiante report = new ReporteEstudiante("st-1", new BigDecimal("12.5"), new BigDecimal("4.0"), 3, LocalDateTime.now());
        when(studentRepository.findByEstudianteId("st-1")).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/reports/student/st-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.estudianteId").value("st-1"))
                .andExpect(jsonPath("$.totalHorasValidadas").value(12.5))
                .andExpect(jsonPath("$.totalHorasPendientes").value(4.0))
                .andExpect(jsonPath("$.totalDocumentos").value(3));
    }

    @Test
    void testGetStudentReportNotFound() throws Exception {
        when(studentRepository.findByEstudianteId("st-unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reports/student/st-unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetGlobalReportSuccess() throws Exception {
        ReporteGlobal global = new ReporteGlobal(10, new BigDecimal("150.0"), new BigDecimal("50.0"), new HashMap<>(), LocalDateTime.now());
        when(globalRepository.findById("global")).thenReturn(Optional.of(global));

        mockMvc.perform(get("/api/reports/global"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalEstudiantes").value(10))
                .andExpect(jsonPath("$.totalHorasValidadas").value(150.0))
                .andExpect(jsonPath("$.totalHorasPendientes").value(50.0));
    }

    @Test
    void testGetGlobalReportNotFound() throws Exception {
        when(globalRepository.findById("global")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reports/global"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testHealth() throws Exception {
        MockMvc appMockMvc = MockMvcBuilders.standaloneSetup(new ReportServiceApplication()).build();
        appMockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("report-service is running"));
    }
}
