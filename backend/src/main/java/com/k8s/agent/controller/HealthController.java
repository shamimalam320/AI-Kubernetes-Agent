package com.k8s.agent.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * Custom health check endpoint
     * 
     * @return Health status with service information
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.debug("Health check requested");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ai-kubernetes-agent");
        response.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(response);
    }
}

