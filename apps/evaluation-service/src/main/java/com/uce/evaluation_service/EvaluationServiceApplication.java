package com.uce.evaluation_service;

import com.uce.evaluation_service.client.UserServiceClient;
import com.uce.evaluation_service.client.UserServiceClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class EvaluationServiceApplication {

    @Autowired(required = false)
    private UserServiceClient userServiceClient;

    public static void main(String[] args) {
        SpringApplication.run(EvaluationServiceApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        String base = "evaluation-service is running";
        if (userServiceClient instanceof UserServiceClientImpl) {
            base += ". Circuit Breaker state: " + ((UserServiceClientImpl) userServiceClient).getCircuitBreakerState();
        }
        return base;
    }
}
