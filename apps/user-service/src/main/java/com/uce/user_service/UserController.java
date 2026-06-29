package com.uce.user_service;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Gestión de perfiles académicos de estudiantes y tutores")
public class UserController {

    @Autowired
    private UserService userService;

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
        return userService.getProfileById(id);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener perfil por Email", description = "Retorna el perfil de usuario correspondiente al correo electrónico institucional provisto.")
    public ResponseEntity<UserProfile> getProfileByEmail(@PathVariable String email) {
        return userService.getProfileByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}