package com.uce.evaluation_service;

import com.uce.evaluation_service.dto.EvaluacionConEstudiante;
import com.uce.evaluation_service.models.EvaluacionFinal;
import com.uce.evaluation_service.services.EvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EvaluationControllerTests {

    private MockMvc mockMvc;

    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private EvaluationController evaluationController;

    private EvaluationServiceApplication application = new EvaluationServiceApplication();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(evaluationController, application).build();
    }

    private String buildJson(String estudianteId, String tutorId, String proyectoId, String calificacion, String comentarios) {
        StringBuilder sb = new StringBuilder("{");
        if (estudianteId != null) sb.append("\"estudianteId\":\"").append(estudianteId).append("\",");
        if (tutorId != null) sb.append("\"tutorId\":\"").append(tutorId).append("\",");
        if (proyectoId != null) sb.append("\"proyectoId\":\"").append(proyectoId).append("\",");
        if (calificacion != null) sb.append("\"calificacion\":").append(calificacion).append(",");
        if (comentarios != null) sb.append("\"comentarios\":\"").append(comentarios).append("\",");
        int last = sb.length() - 1;
        if (sb.charAt(last) == ',') sb.deleteCharAt(last);
        sb.append("}");
        return sb.toString();
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("evaluation-service is running"));
    }

    @Test
    public void testCreateEvaluation_Success() throws Exception {
        EvaluacionFinal eval = new EvaluacionFinal();
        eval.setId(1L);
        eval.setEstudianteId("101");
        eval.setTutorId("202");
        eval.setProyectoId("50");
        eval.setCalificacion(new BigDecimal("8.5"));
        eval.setComentarios("Excelente");
        eval.setFechaEvaluacion(LocalDate.now());

        Mockito.when(evaluationService.createEvaluation(any(EvaluacionFinal.class))).thenReturn(eval);

        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson("101", "202", "50", "8.5", "Excelente")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estudianteId").value("101"))
                .andExpect(jsonPath("$.tutorId").value("202"))
                .andExpect(jsonPath("$.proyectoId").value("50"))
                .andExpect(jsonPath("$.calificacion").value(8.5));
    }

    @Test
    public void testCreateEvaluation_MissingEstudianteId() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson(null, "202", "50", "8.5", "Excelente")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El ID del estudiante es obligatorio."));
    }

    @Test
    public void testCreateEvaluation_MissingTutorId() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson("101", null, "50", "8.5", "Excelente")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El ID del tutor es obligatorio."));
    }

    @Test
    public void testCreateEvaluation_MissingProyectoId() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson("101", "202", null, "8.5", "Excelente")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El ID del proyecto es obligatorio."));
    }

    @Test
    public void testCreateEvaluation_MissingCalificacion() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson("101", "202", "50", null, "Excelente")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La calificación es obligatoria."));
    }

    @Test
    public void testCreateEvaluation_CalificacionBelowZero() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson("101", "202", "50", "-0.1", "Excelente")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La calificación debe estar entre 0 y 10."));
    }

    @Test
    public void testCreateEvaluation_CalificacionAboveTen() throws Exception {
        mockMvc.perform(post("/api/evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildJson("101", "202", "50", "10.01", "Excelente")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La calificación debe estar entre 0 y 10."));
    }

    @Test
    public void testGetEvaluationById_Exists() throws Exception {
        EvaluacionFinal eval = new EvaluacionFinal();
        eval.setId(1L);
        eval.setEstudianteId("101");
        eval.setTutorId("202");
        eval.setProyectoId("50");
        eval.setCalificacion(new BigDecimal("9.00"));
        eval.setComentarios("Muy bueno");

        Mockito.when(evaluationService.getEvaluationById(1L)).thenReturn(Optional.of(eval));

        mockMvc.perform(get("/api/evaluations/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estudianteId").value("101"))
                .andExpect(jsonPath("$.calificacion").value(9.00));
    }

    @Test
    public void testGetEvaluationById_NotExists() throws Exception {
        Mockito.when(evaluationService.getEvaluationById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/evaluations/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEvaluationsByStudentId() throws Exception {
        EvaluacionFinal eval = new EvaluacionFinal();
        eval.setId(1L);
        eval.setEstudianteId("101");
        eval.setTutorId("202");
        eval.setProyectoId("50");
        eval.setCalificacion(new BigDecimal("8.50"));
        eval.setComentarios("Excelente");
        eval.setCreatedAt(LocalDateTime.of(2026, 6, 15, 10, 0, 0));

        EvaluacionConEstudiante dto = new EvaluacionConEstudiante(eval, "Gissela Muzo", "Sistemas");

        Mockito.when(evaluationService.getEvaluationsByStudentId("101")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/evaluations/student/101")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estudianteId").value("101"))
                .andExpect(jsonPath("$[0].nombre").value("Gissela Muzo"))
                .andExpect(jsonPath("$[0].carrera").value("Sistemas"));
    }
}
