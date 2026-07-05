package com.uce.user_service;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.models.ProfileUpdateDTO;
import com.uce.user_service.services.UserService;
import com.uce.user_service.repositories.UserProfileRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class UserProfileControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void test_getProfile_exitoso() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFirstName("Gissela");
        profile.setEmail("gisse@uce.edu.ec");

        Mockito.when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        mockMvc.perform(get("/api/users/profile/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Gissela"))
                .andExpect(jsonPath("$.email").value("gisse@uce.edu.ec"));
    }

    @Test
    public void test_getProfile_no_existe() throws Exception {
        Mockito.when(userProfileRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/profile/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_updateProfile_exitoso() throws Exception {
        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setId(1L);
        updatedProfile.setFirstName("Gissela Modificada");
        updatedProfile.setPhone("0999999999");

        Mockito.when(userService.updateProfile(Mockito.eq(1L), Mockito.any(ProfileUpdateDTO.class)))
                .thenReturn(updatedProfile);

        String jsonBody = "{\"firstName\":\"Gissela Modificada\",\"phone\":\"0999999999\"}";

        mockMvc.perform(put("/api/users/profile/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Gissela Modificada"))
                .andExpect(jsonPath("$.phone").value("0999999999"));
    }

    @Test
    public void test_deleteProfile_exitoso() throws Exception {
        Mockito.doNothing().when(userProfileRepository).deleteById(1L);

        mockMvc.perform(delete("/api/users/profile/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
