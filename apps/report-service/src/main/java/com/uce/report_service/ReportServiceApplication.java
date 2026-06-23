package com.uce.report_service;

import com.uce.report_service.consumers.KafkaReportConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ReportServiceApplication {

    @Autowired(required = false)
    private KafkaReportConsumer kafkaReportConsumer;

    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        String base = "report-service is running";
        if (kafkaReportConsumer != null && kafkaReportConsumer.getCircuitBreakerState() != null) {
            base += ". Circuit Breaker state: " + kafkaReportConsumer.getCircuitBreakerState();
        }
        return base;
    }
}
