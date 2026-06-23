package com.uce.document_service;

import com.uce.document_service.services.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DocumentServiceApplication {

    @Autowired(required = false)
    private DocumentService documentService;

    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        String base = "document-service is running";
        if (documentService != null && documentService.getCircuitBreakerState() != null) {
            base += ". Circuit Breaker state: " + documentService.getCircuitBreakerState();
        }
        return base;
    }
}
