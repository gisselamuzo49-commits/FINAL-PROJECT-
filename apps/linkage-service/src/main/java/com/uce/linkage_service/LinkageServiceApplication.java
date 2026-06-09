package com.uce.linkage_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class LinkageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinkageServiceApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        return "linkage-service is running";
    }
}