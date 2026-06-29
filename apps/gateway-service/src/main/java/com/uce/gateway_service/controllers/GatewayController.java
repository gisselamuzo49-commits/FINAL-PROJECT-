package com.uce.gateway_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.uce.gateway_service.etcd.EtcdConfigService etcdConfigService;

    @GetMapping("/dynamodb/stats")
    public ResponseEntity<Map<String, String>> getDynamoDbStats() {
        return ResponseEntity.ok(Map.of(
            "database", "DynamoDB",
            "table", "jwt-blacklist",
            "status", "connected"
        ));
    }

    @GetMapping("/etcd/stats")
    public ResponseEntity<String> getEtcdStats() {
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(etcdConfigService.getDatabaseInfo());
    }
}
