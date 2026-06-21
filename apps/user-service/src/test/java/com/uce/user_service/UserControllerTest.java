package com.uce.user_service;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.services.UserService;
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

public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetProfileByEmail_Exists() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFirstName("Gissela");
        profile.setLastName("Muzo");
        profile.setEmail("gisse.muzo@uce.edu.ec");
        profile.setRole("STUDENT");
        profile.setCarrera("Sistemas");

        Mockito.when(userService.getProfileByEmail("gisse.muzo@uce.edu.ec")).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/users/email/gisse.muzo@uce.edu.ec")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Gissela"))
                .andExpect(jsonPath("$.lastName").value("Muzo"))
                .andExpect(jsonPath("$.email").value("gisse.muzo@uce.edu.ec"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.carrera").value("Sistemas"));
    }

    @Test
    public void testGetProfileByEmail_NotExists() throws Exception {
        Mockito.when(userService.getProfileByEmail("nonexistent@uce.edu.ec")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/email/nonexistent@uce.edu.ec")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
