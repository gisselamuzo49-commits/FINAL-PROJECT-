package com.uce.linkage_service;

import com.uce.linkage_service.models.LinkageProject;
import com.uce.linkage_service.services.LinkageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class LinkageControllerTests {

    private MockMvc mockMvc;

    @Mock
    private LinkageService linkageService;

    @InjectMocks
    private LinkageController linkageController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(linkageController).build();
    }

    @Test
    public void testGetProjectById_Exists() throws Exception {
        LinkageProject project = new LinkageProject();
        project.setId(1L);
        project.setName("Digital Literacy");
        project.setInstitution("GAD Calderon");
        project.setStatus("PLANNED");

        Mockito.when(linkageService.getProjectById(1L)).thenReturn(Optional.of(project));

        mockMvc.perform(get("/api/linkage/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Digital Literacy"))
                .andExpect(jsonPath("$.institution").value("GAD Calderon"));
    }

    @Test
    public void testGetProjectById_NotExists() throws Exception {
        Mockito.when(linkageService.getProjectById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/linkage/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
