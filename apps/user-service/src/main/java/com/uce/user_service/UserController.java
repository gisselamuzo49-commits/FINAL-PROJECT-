package com.uce.user_service;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import com.uce.user_service.models.ProfileUpdateDTO;
import com.uce.user_service.repositories.UserProfileRepository;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestión de perfiles académicos de estudiantes y tutores")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Usuarios (Spring Boot)!";
    }

    @PostMapping
    @Operation(summary = "Crear perfil de usuario", description = "Registra un nuevo perfil de usuario (estudiante o tutor) con sus datos académicos correspondientes.")
    public UserProfile createProfile(@RequestBody UserProfile profile) {
        return userService.createProfile(profile);
    }

    @GetMapping
    @Operation(summary = "Listar todos los perfiles", description = "Retorna una lista con todos los perfiles de usuario registrados en el sistema.")
    public List<UserProfile> getAllProfiles() {
        return userService.getAllProfiles();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil por ID", description = "Retorna el perfil de usuario correspondiente al ID provisto.")
    public UserProfile getProfileById(@PathVariable Long id) {
        logger.info("Fetching user profile: {}", id);
        return userService.getProfileById(id);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener perfil por Email", description = "Retorna el perfil de usuario correspondiente al correo electrónico institucional provisto.")
    public ResponseEntity<UserProfile> getProfileByEmail(@PathVariable String email) {
        return userService.getProfileByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET perfil por ID
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable Long id) {
        return userProfileRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // PUT actualizar perfil
    @PutMapping("/profile/{id}")
    public ResponseEntity<UserProfile> updateProfile(
        @PathVariable Long id,
        @RequestBody ProfileUpdateDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(id, dto));
    }

    // DELETE eliminar perfil
    @DeleteMapping("/profile/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        userProfileRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}