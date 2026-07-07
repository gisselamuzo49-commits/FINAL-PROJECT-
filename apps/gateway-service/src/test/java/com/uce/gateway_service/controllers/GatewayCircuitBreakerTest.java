package com.uce.gateway_service.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GatewayCircuitBreakerTest {

    @Test
    public void testFallbackEndpoint_Returns503AndErrorPayload() {
        GatewayFallbackController controller = new GatewayFallbackController();
        ResponseEntity<?> response = controller.fallback();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(503, body.get("status"));
        assertEquals("Servicio no disponible temporalmente", body.get("error"));
    }
}
