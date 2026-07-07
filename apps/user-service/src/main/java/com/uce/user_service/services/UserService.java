package com.uce.user_service.services;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.repositories.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Collections;

import com.uce.user_service.models.ProfileUpdateDTO;

@Service
public class UserService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfile createProfile(UserProfile profile) {
        return userProfileRepository.save(profile);
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getProfilesFallback")
    public List<UserProfile> getAllProfiles() {
        return userProfileRepository.findAll();
    }

    public List<UserProfile> getProfilesFallback(Throwable t) {
        return Collections.emptyList();
    }

    public UserProfile getProfileById(Long id) {
        return userProfileRepository.findById(id).orElse(null);
    }

    public Optional<UserProfile> getProfileByEmail(String email) {
        return userProfileRepository.findByEmail(email);
    }

    public UserProfile updateProfile(Long id, ProfileUpdateDTO dto) {
        UserProfile profile = userProfileRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        if (dto.getFirstName() != null) profile.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) profile.setLastName(dto.getLastName());
        if (dto.getPhone() != null) profile.setPhone(dto.getPhone());
        if (dto.getCarrera() != null) profile.setCarrera(dto.getCarrera());
        if (dto.getFacultad() != null) profile.setFacultad(dto.getFacultad());
        if (dto.getHabilidades() != null) profile.setHabilidades(dto.getHabilidades());
        if (dto.getCursos() != null) profile.setCursos(dto.getCursos());
        if (dto.getExperiencia() != null) profile.setExperiencia(dto.getExperiencia());
        if (dto.getDescripcion() != null) profile.setDescripcion(dto.getDescripcion());
        
        return userProfileRepository.save(profile);
    }
}