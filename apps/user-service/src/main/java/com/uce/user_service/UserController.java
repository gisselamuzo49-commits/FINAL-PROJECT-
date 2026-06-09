package com.uce.user_service;

import com.uce.user_service.models.UserProfile;
import com.uce.user_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/hello")
    public String sayHello() {
        return "¡Hola desde el Backend de Usuarios (Spring Boot)!";
    }

    @PostMapping
    public UserProfile createProfile(@RequestBody UserProfile profile) {
        return userService.createProfile(profile);
    }

    @GetMapping
    public List<UserProfile> getAllProfiles() {
        return userService.getAllProfiles();
    }

    @GetMapping("/{id}")
    public UserProfile getProfileById(@PathVariable Long id) {
        return userService.getProfileById(id);
    }
}