package com.uce.user_service.services;

import com.uce.user_service.models.ProfileUpdateDTO;
import com.uce.user_service.models.UserProfile;
import com.uce.user_service.repositories.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createProfileSavesAndReturnsProfile() {
        UserProfile profile = profile();
        when(userProfileRepository.save(profile)).thenReturn(profile);

        UserProfile result = userService.createProfile(profile);

        assertSame(profile, result);
        verify(userProfileRepository).save(profile);
    }

    @Test
    void getAllProfilesReturnsRepositoryProfiles() {
        List<UserProfile> profiles = List.of(profile());
        when(userProfileRepository.findAll()).thenReturn(profiles);

        List<UserProfile> result = userService.getAllProfiles();

        assertSame(profiles, result);
        verify(userProfileRepository).findAll();
    }

    @Test
    void getProfilesFallbackReturnsEmptyList() {
        List<UserProfile> result = userService.getProfilesFallback(new RuntimeException("offline"));

        assertEquals(List.of(), result);
        verifyNoInteractions(userProfileRepository);
    }

    @Test
    void getProfileByIdReturnsProfileWhenFound() {
        UserProfile profile = profile();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));

        UserProfile result = userService.getProfileById(1L);

        assertSame(profile, result);
        verify(userProfileRepository).findById(1L);
    }

    @Test
    void getProfileByIdReturnsNullWhenMissing() {
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        UserProfile result = userService.getProfileById(99L);

        assertNull(result);
        verify(userProfileRepository).findById(99L);
    }

    @Test
    void getProfileByEmailReturnsRepositoryResult() {
        UserProfile profile = profile();
        when(userProfileRepository.findByEmail("student@uce.edu.ec"))
                .thenReturn(Optional.of(profile));

        Optional<UserProfile> result = userService.getProfileByEmail("student@uce.edu.ec");

        assertEquals(Optional.of(profile), result);
        verify(userProfileRepository).findByEmail("student@uce.edu.ec");
    }

    @Test
    void updateProfileAppliesEveryMutableField() {
        UserProfile profile = profile();
        ProfileUpdateDTO update = completeUpdate();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(profile)).thenReturn(profile);

        UserProfile result = userService.updateProfile(1L, update);

        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(captor.capture());
        UserProfile saved = captor.getValue();
        assertSame(profile, result);
        assertEquals("Updated", saved.getFirstName());
        assertEquals("Student", saved.getLastName());
        assertEquals("0999999999", saved.getPhone());
        assertEquals("Software", saved.getCarrera());
        assertEquals("Engineering", saved.getFacultad());
        assertEquals("Java, Spring", saved.getHabilidades());
        assertEquals("Testing", saved.getCursos());
        assertEquals("Two years", saved.getExperiencia());
        assertEquals("Backend developer", saved.getDescripcion());
        assertEquals("student@uce.edu.ec", saved.getEmail());
        assertEquals("STUDENT", saved.getRole());
    }

    @Test
    void updateProfilePreservesFieldsWhenUpdateValuesAreNull() {
        UserProfile profile = profile();
        ProfileUpdateDTO update = new ProfileUpdateDTO();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(userProfileRepository.save(profile)).thenReturn(profile);

        UserProfile result = userService.updateProfile(1L, update);

        assertSame(profile, result);
        assertEquals("Original", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("0987654321", result.getPhone());
        assertEquals("Information Systems", result.getCarrera());
        assertEquals("FICA", result.getFacultad());
        assertEquals("SQL", result.getHabilidades());
        assertEquals("Databases", result.getCursos());
        assertEquals("Intern", result.getExperiencia());
        assertEquals("Student profile", result.getDescripcion());
        verify(userProfileRepository).save(profile);
    }

    @Test
    void updateProfileThrowsWhenProfileDoesNotExist() {
        ProfileUpdateDTO update = completeUpdate();
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> userService.updateProfile(99L, update));

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(userProfileRepository).findById(99L);
    }

    private static UserProfile profile() {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setFirstName("Original");
        profile.setLastName("User");
        profile.setEmail("student@uce.edu.ec");
        profile.setPhone("0987654321");
        profile.setRole("STUDENT");
        profile.setCarrera("Information Systems");
        profile.setFacultad("FICA");
        profile.setHabilidades("SQL");
        profile.setCursos("Databases");
        profile.setExperiencia("Intern");
        profile.setDescripcion("Student profile");
        return profile;
    }

    private static ProfileUpdateDTO completeUpdate() {
        ProfileUpdateDTO update = new ProfileUpdateDTO();
        update.setFirstName("Updated");
        update.setLastName("Student");
        update.setPhone("0999999999");
        update.setCarrera("Software");
        update.setFacultad("Engineering");
        update.setHabilidades("Java, Spring");
        update.setCursos("Testing");
        update.setExperiencia("Two years");
        update.setDescripcion("Backend developer");
        return update;
    }
}
