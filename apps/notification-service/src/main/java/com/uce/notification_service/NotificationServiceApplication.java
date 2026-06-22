package com.uce.notification_service;

import com.uce.notification_service.config.MqttClientManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class NotificationServiceApplication {

    @Autowired(required = false)
    private MqttClientManager mqttClientManager;

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        String base = "notification-service is running";
        if (mqttClientManager != null && mqttClientManager.getCircuitBreakerState() != null) {
            base += ". Circuit Breaker state: " + mqttClientManager.getCircuitBreakerState();
        }
        return base;
    }
}
