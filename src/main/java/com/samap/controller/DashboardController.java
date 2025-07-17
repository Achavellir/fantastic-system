package com.samap.controller;

import com.samap.config.AuditAspect.Auditable;
import com.samap.service.AuditService;
import com.samap.service.RiskAssessmentService;
import com.samap.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard controller providing system overview and analytics
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final AuditService auditService;
    private final RiskAssessmentService riskAssessmentService;

    /**
     * Get dashboard overview
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('USER')")
    @Auditable(action = "DASHBOARD_VIEW", resource = "DASHBOARD")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(Authentication authentication) {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            // User statistics
            var userStats = userService.getUserStatistics();
            overview.put("userStatistics", Map.of(
                "total", userStats.getTotalUsers(),
                "active", userStats.getActiveUsers(),
                "locked", userStats.getLockedUsers(),
                "inactive", userStats.getInactiveUsers()
            ));

            // Activity statistics (last 24 hours)
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            var activityStats = auditService.getActivityStatistics(since);
            overview.put("activityStatistics", Map.of(
                "total", activityStats.getTotalActivities(),
                "successful", activityStats.getSuccessfulActivities(),
                "failed", activityStats.getFailedActivities(),
                "highRisk", activityStats.getHighRiskActivities(),
                "anomalous", activityStats.getAnomalousActivities()
            ));

            // Top active users (last 24 hours)
            var topUsers = auditService.getTopActiveUsers(since, 
                org.springframework.data.domain.PageRequest.of(0, 5));
            overview.put("topActiveUsers", topUsers);

            // Hourly activity trends
            var hourlyTrends = auditService.getActivityTrendsByHour(since);
            overview.put("hourlyActivityTrends", hourlyTrends);

            // System status
            overview.put("systemStatus", Map.of(
                "status", "OPERATIONAL",
                "uptime", getSystemUptime(),
                "timestamp", LocalDateTime.now()
            ));

            // User-specific data
            if (authentication != null) {
                String username = authentication.getName();
                
                // User risk assessment
                var riskReport = riskAssessmentService.generateRiskReport(username, since);
                overview.put("userRiskAssessment", Map.of(
                    "riskLevel", riskReport.getRiskLevel(),
                    "averageRiskScore", riskReport.getAverageRiskScore(),
                    "totalActivities", riskReport.getTotalActivities(),
                    "highRiskActivities", riskReport.getHighRiskActivities(),
                    "anomalousActivities", riskReport.getAnomalousActivities()
                ));
            }

        } catch (Exception e) {
            log.error("Error generating dashboard overview: {}", e.getMessage(), e);
            overview.put("error", "Unable to load complete dashboard data");
        }

        return ResponseEntity.ok(overview);
    }

    /**
     * Get security metrics
     */
    @GetMapping("/security-metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('SECURITY_OFFICER')")
    @Auditable(action = "SECURITY_METRICS_VIEW", resource = "DASHBOARD")
    public ResponseEntity<Map<String, Object>> getSecurityMetrics(
            @RequestParam(defaultValue = "24") int hours) {
        
        Map<String, Object> metrics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        try {
            // Failed login attempts
            var failedLogins = auditService.getFailedLoginAttempts(
                org.springframework.data.domain.PageRequest.of(0, 10));
            metrics.put("recentFailedLogins", failedLogins.getContent());
            metrics.put("failedLoginCount", failedLogins.getTotalElements());

            // High-risk activities
            var highRiskActivities = auditService.getHighRiskActivities(
                org.springframework.data.domain.PageRequest.of(0, 10));
            metrics.put("highRiskActivities", highRiskActivities.getContent());
            metrics.put("highRiskCount", highRiskActivities.getTotalElements());

            // Anomalous activities
            var anomalousActivities = auditService.getAnomalousActivities(
                org.springframework.data.domain.PageRequest.of(0, 10));
            metrics.put("anomalousActivities", anomalousActivities.getContent());
            metrics.put("anomalousCount", anomalousActivities.getTotalElements());

            // Activity statistics
            var activityStats = auditService.getActivityStatistics(since);
            metrics.put("activityStatistics", activityStats);

            metrics.put("period", hours + " hours");
            metrics.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error generating security metrics: {}", e.getMessage(), e);
            metrics.put("error", "Unable to load security metrics");
        }

        return ResponseEntity.ok(metrics);
    }

    /**
     * Get user activity summary
     */
    @GetMapping("/user-activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "USER_ACTIVITY_VIEW", resource = "DASHBOARD")
    public ResponseEntity<Map<String, Object>> getUserActivitySummary(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> summary = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        try {
            // Top active users
            var topUsers = auditService.getTopActiveUsers(since, 
                org.springframework.data.domain.PageRequest.of(0, limit));
            summary.put("topActiveUsers", topUsers);

            // Activity trends
            var hourlyTrends = auditService.getActivityTrendsByHour(since);
            summary.put("hourlyTrends", hourlyTrends);

            summary.put("period", hours + " hours");
            summary.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error generating user activity summary: {}", e.getMessage(), e);
            summary.put("error", "Unable to load user activity data");
        }

        return ResponseEntity.ok(summary);
    }

    /**
     * Get system health status
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "SYSTEM_HEALTH_VIEW", resource = "DASHBOARD")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Database connectivity check
            var userStats = userService.getUserStatistics();
            health.put("database", Map.of(
                "status", "UP",
                "totalUsers", userStats.getTotalUsers()
            ));

            // Memory usage
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            health.put("memory", Map.of(
                "max", maxMemory / (1024 * 1024) + " MB",
                "total", totalMemory / (1024 * 1024) + " MB",
                "used", usedMemory / (1024 * 1024) + " MB",
                "free", freeMemory / (1024 * 1024) + " MB",
                "usagePercentage", (usedMemory * 100) / maxMemory
            ));

            // System uptime
            health.put("uptime", getSystemUptime());
            health.put("status", "HEALTHY");
            health.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error checking system health: {}", e.getMessage(), e);
            health.put("status", "DEGRADED");
            health.put("error", e.getMessage());
        }

        return ResponseEntity.ok(health);
    }

    /**
     * Get system uptime (simplified implementation)
     */
    private String getSystemUptime() {
        long uptimeMillis = System.currentTimeMillis() - 
            java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d days, %d hours, %d minutes", 
            days, hours % 24, minutes % 60);
    }
}
