package com.uce.internship_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.internship_service.controllers.ApplicationRequest;
import com.uce.internship_service.controllers.PostulacionController;
import com.uce.internship_service.controllers.StatusUpdateRequest;
import com.uce.internship_service.repositories.EstudianteAplicanteDto;
import com.uce.internship_service.repositories.StudentApplicationDto;
import com.uce.internship_service.services.PostulacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostulacionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PostulacionService postulacionService;

    @InjectMocks
    private PostulacionController postulacionController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(postulacionController).build();
    }

    @Test
    public void test_crear_postulacion_exitoso() throws Exception {
        ApplicationRequest request = new ApplicationRequest("EST-123", "Mensaje de postulación");
        StudentApplicationDto responseDto = new StudentApplicationDto(
                "1", "Pasantía Java", "Company A", 100L, "PENDIENTE", "Mensaje de postulación", LocalDateTime.now()
        );

        Mockito.when(postulacionService.createApplication(eq(1L), eq("EST-123"), eq("Mensaje de postulación")))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/internships/1/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postulacionId").value(100))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    public void test_listar_aplicantes() throws Exception {
        EstudianteAplicanteDto app1 = new EstudianteAplicanteDto("EST-1", 10L, "PENDIENTE", "Mensaje 1", LocalDateTime.now());
        EstudianteAplicanteDto app2 = new EstudianteAplicanteDto("EST-2", 11L, "ACEPTADA", "Mensaje 2", LocalDateTime.now());

        Mockito.when(postulacionService.getApplicantsForInternship(1L)).thenReturn(Arrays.asList(app1, app2));

        mockMvc.perform(get("/api/internships/1/applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].estudianteId").value("EST-1"))
                .andExpect(jsonPath("$[1].estudianteId").value("EST-2"));
    }

    @Test
    public void test_actualizar_estado_aceptar() throws Exception {
        StatusUpdateRequest request = new StatusUpdateRequest("ACEPTADA");
        StudentApplicationDto responseDto = new StudentApplicationDto(
                "1", "Pasantía Java", "Company A", 100L, "ACEPTADA", "Mensaje", LocalDateTime.now()
        );

        Mockito.when(postulacionService.updateStatus(eq(100L), eq("ACEPTADA"))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/internships/applications/100/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postulacionId").value(100))
                .andExpect(jsonPath("$.estado").value("ACEPTADA"));
    }

    @Test
    public void test_actualizar_estado_rechazar() throws Exception {
        StatusUpdateRequest request = new StatusUpdateRequest("RECHAZADA");
        StudentApplicationDto responseDto = new StudentApplicationDto(
                "1", "Pasantía Java", "Company A", 100L, "RECHAZADA", "Mensaje", LocalDateTime.now()
        );

        Mockito.when(postulacionService.updateStatus(eq(100L), eq("RECHAZADA"))).thenReturn(responseDto);

        mockMvc.perform(patch("/api/internships/applications/100/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postulacionId").value(100))
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
    }

    @Test
    public void test_postulacion_oferta_no_existe() throws Exception {
        ApplicationRequest request = new ApplicationRequest("EST-123", "Mensaje de postulación");

        Mockito.when(postulacionService.createApplication(eq(99L), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta no encontrada"));

        mockMvc.perform(post("/api/internships/99/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
