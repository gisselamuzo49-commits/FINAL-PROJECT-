package com.uce.hours_service;

import com.uce.hours_service.client.UserServiceClient;
import com.uce.hours_service.client.UserServiceClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@RestController
public class HoursServiceApplication {

    @Autowired(required = false)
    private UserServiceClient userServiceClient;

    public static void main(String[] args) {
        SpringApplication.run(HoursServiceApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        String base = "hours-service is running";
        if (userServiceClient instanceof UserServiceClientImpl) {
            base += ". Circuit Breaker state: " + ((UserServiceClientImpl) userServiceClient).getCircuitBreakerState();
        }
        return base;
    }
}
