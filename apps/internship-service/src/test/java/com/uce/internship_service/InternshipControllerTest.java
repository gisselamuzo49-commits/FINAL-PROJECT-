package com.uce.internship_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uce.internship_service.controllers.InternshipController;
import com.uce.internship_service.models.Internship;
import com.uce.internship_service.services.InternshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InternshipControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InternshipService internshipService;

    @InjectMocks
    private InternshipController internshipController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(internshipController).build();
    }

    @Test
    public void test_getAll_retorna_lista() throws Exception {
        Internship intern1 = new Internship("Title 1", "Company 1", "Desc 1", "ABIERTA");
        Internship intern2 = new Internship("Title 2", "Company 2", "Desc 2", "ABIERTA");

        Mockito.when(internshipService.getAllInternships()).thenReturn(Arrays.asList(intern1, intern2));

        mockMvc.perform(get("/api/internships")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Title 1"))
                .andExpect(jsonPath("$[1].title").value("Title 2"));
    }

    @Test
    public void test_create_exitoso() throws Exception {
        Internship intern = new Internship("Title 1", "Company 1", "Desc 1", "ABIERTA");
        Mockito.when(internshipService.createInternship(Mockito.any(Internship.class))).thenReturn(intern);

        mockMvc.perform(post("/api/internships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(intern)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title 1"))
                .andExpect(jsonPath("$.company").value("Company 1"));
    }

    @Test
    public void test_create_campos_vacios() throws Exception {
        Internship intern = new Internship("", "", "Desc 1", "ABIERTA");

        mockMvc.perform(post("/api/internships")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(intern)))
                .andExpect(status().isBadRequest());
    }
}
