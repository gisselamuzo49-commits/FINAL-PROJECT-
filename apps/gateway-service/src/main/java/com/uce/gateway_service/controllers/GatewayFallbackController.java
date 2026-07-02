package com.uce.gateway_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GatewayFallbackController {

    @GetMapping("/fallback")
    public ResponseEntity<?> fallback() {
        return ResponseEntity.status(503)
                .body(Map.of(
                        "error", "Servicio no disponible temporalmente",
                        "status", 503
                ));
    }
}
