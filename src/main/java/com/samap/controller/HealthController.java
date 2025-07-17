package com.samap.controller;

import com.samap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Public health and system information controller
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class HealthController {

    private final UserService userService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "SAMAP - Secure Access Management & Audit Platform");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "SAMAP");
        response.put("description", "Secure Access Management & Audit Platform");
        response.put("version", "1.0.0");
        response.put("features", new String[]{
            "JWT Authentication",
            "Role-Based Access Control",
            "Comprehensive Audit Logging",
            "Risk Assessment",
            "Anomaly Detection",
            "Real-time Monitoring"
        });

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> publicStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            var userStats = userService.getUserStatistics();
            response.put("totalUsers", userStats.getTotalUsers());
            response.put("activeUsers", userStats.getActiveUsers());
            response.put("systemStatus", "OPERATIONAL");
        } catch (Exception e) {
            response.put("systemStatus", "DEGRADED");
        }

        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
