package com.uce.hours_service;

import com.uce.hours_service.models.EstadoHoras;
import com.uce.hours_service.models.RegistroHoras;
import com.uce.hours_service.models.HorasResumen;
import com.uce.hours_service.cqrs.HoursCommandService;
import com.uce.hours_service.cqrs.HoursQueryService;
import com.uce.hours_service.cqrs.commands.CreateHoursCommand;
import com.uce.hours_service.cqrs.commands.ValidateHoursCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HoursControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HoursCommandService commandService;

    @Mock
    private HoursQueryService queryService;

    @InjectMocks
    private HoursController hoursController;

    private HoursServiceApplication hoursServiceApplication = new HoursServiceApplication();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(hoursController, hoursServiceApplication).build();
    }

    // --- Helper to build a minimal valid JSON body ---
    private String buildJson(String estudianteId, String proyectoId, String fecha, String horas, String desc) {
        StringBuilder sb = new StringBuilder("{");
        if (estudianteId != null) sb.append("\"estudianteId\":\"").append(estudianteId).append("\",");
        if (proyectoId  != null) sb.append("\"proyectoId\":\"").append(proyectoId).append("\",");
        if (fecha       != null) sb.append("\"fecha\":\"").append(fecha).append("\",");
        if (horas       != null) sb.append("\"horas\":").append(horas).append(",");
        if (desc        != null) sb.append("\"descripcionActividad\":\"").append(desc).append("\",");
        // remove trailing comma if any
        int last = sb.length() - 1;
        if (sb.charAt(last) == ',') sb.deleteCharAt(last);
        sb.append("}");
        return sb.toString();
    }

    @Test
    void health_ReturnsStatusOkAndText() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("hours-service is running"));
    }

    @Test
    void createHoursRegistration_WithValidData_ReturnsOk() throws Exception {
        RegistroHoras mockRegistro = new RegistroHoras();
        mockRegistro.setId(1L);
        mockRegistro.setEstudianteId("100");
        mockRegistro.setProyectoId("50");
        mockRegistro.setFecha(LocalDate.of(2026, 6, 10));
        mockRegistro.setHoras(4.5);
        mockRegistro.setDescripcionActividad("Actividad de prueba");
        mockRegistro.setEstado(EstadoHoras.PENDIENTE);

        Mockito.when(commandService.createHoursRegistration(any(CreateHoursCommand.class)))
                .thenReturn(mockRegistro);

        mockMvc.perform(post("/api/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("100", "50", "2026-06-10", "4.5", "Actividad de prueba")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.estudianteId").value("100"))
                .andExpect(jsonPath("$.proyectoId").value("50"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void createHoursRegistration_MissingEstudianteId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson(null, "50", "2026-06-10", "4.5", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El ID del estudiante es obligatorio."));
    }

    @Test
    void createHoursRegistration_MissingProyectoId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("100", null, "2026-06-10", "4.5", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El ID del proyecto es obligatorio."));
    }

    @Test
    void createHoursRegistration_MissingFecha_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("100", "50", null, "4.5", null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La fecha es obligatoria."));
    }

    @Test
    void createHoursRegistration_MissingHoras_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("100", "50", "2026-06-10", null, null)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El numero de horas es obligatorio."));
    }

    @Test
    void validarHoursRegistration_WithAprobado_ReturnsValidado() throws Exception {
        RegistroHoras mockRegistro = new RegistroHoras();
        mockRegistro.setId(1L);
        mockRegistro.setEstudianteId("100");
        mockRegistro.setTutorId("200");
        mockRegistro.setEstado(EstadoHoras.VALIDADO);

        Mockito.when(commandService.validarHoursRegistration(any(ValidateHoursCommand.class)))
                .thenReturn(Optional.of(mockRegistro));

        mockMvc.perform(patch("/api/hours/1/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tutorId\":\"200\",\"aprobado\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tutorId").value("200"))
                .andExpect(jsonPath("$.estado").value("VALIDADO"));
    }

    @Test
    void validarHoursRegistration_NonExistentId_ReturnsNotFound() throws Exception {
        Mockito.when(commandService.validarHoursRegistration(any(ValidateHoursCommand.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/hours/999/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tutorId\":\"200\",\"aprobado\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validarHoursRegistration_MissingTutorId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/hours/1/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprobado\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El ID del tutor es obligatorio."));
    }

    @Test
    void validarHoursRegistration_MissingAprobado_ReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/hours/1/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tutorId\":\"200\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El campo aprobado es obligatorio."));
    }

    @Test
    void getStudentSummary_ExistentStudent_ReturnsOk() throws Exception {
        HorasResumen mockResumen = new HorasResumen("100");
        mockResumen.setTotalHorasValidadas(10.0);
        mockResumen.setTotalHorasPendientes(5.0);

        Mockito.when(queryService.getStudentSummary("100"))
                .thenReturn(Optional.of(mockResumen));

        mockMvc.perform(get("/api/hours/student/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estudianteId").value("100"))
                .andExpect(jsonPath("$.totalHorasValidadas").value(10.0))
                .andExpect(jsonPath("$.totalHorasPendientes").value(5.0));
    }

    @Test
    void getStudentSummary_NonExistentStudent_ReturnsNotFound() throws Exception {
        Mockito.when(queryService.getStudentSummary("999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/hours/student/999"))
                .andExpect(status().isNotFound());
    }
}
