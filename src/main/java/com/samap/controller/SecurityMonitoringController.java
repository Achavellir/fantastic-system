package com.samap.controller;

import com.samap.config.AuditAspect.Auditable;
import com.samap.service.NotificationService;
import com.samap.service.SecurityMonitoringService;
import com.samap.service.SecurityMonitoringService.SecurityAlert;
import com.samap.service.SecurityMonitoringService.SecurityStatus;
import com.samap.service.NotificationService.AlertStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Security monitoring controller for real-time threat detection and alerting
 */
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
@Slf4j
public class SecurityMonitoringController {

    private final SecurityMonitoringService securityMonitoringService;
    private final NotificationService notificationService;

    /**
     * Get current security status
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_OFFICER') or hasRole('AUDITOR')")
    @Auditable(action = "SECURITY_STATUS_VIEW", resource = "SECURITY")
    public ResponseEntity<SecurityStatus> getSecurityStatus() {
        SecurityStatus status = securityMonitoringService.getCurrentSecurityStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Get recent security alerts
     */
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_OFFICER') or hasRole('AUDITOR')")
    @Auditable(action = "SECURITY_ALERTS_VIEW", resource = "SECURITY")
    public ResponseEntity<List<SecurityAlert>> getRecentAlerts(
            @RequestParam(defaultValue = "20") int limit) {
        
        List<SecurityAlert> alerts = notificationService.getRecentAlerts(limit);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alerts by severity
     */
    @GetMapping("/alerts/severity/{severity}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_OFFICER') or hasRole('AUDITOR')")
    @Auditable(action = "SECURITY_ALERTS_FILTER", resource = "SECURITY", logParameters = true)
    public ResponseEntity<List<SecurityAlert>> getAlertsBySeverity(
            @PathVariable String severity,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<SecurityAlert> alerts = notificationService.getAlertsBySeverity(severity, limit);
        return ResponseEntity.ok(alerts);
    }

    /**
     * Get alert statistics
     */
    @GetMapping("/alerts/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_OFFICER') or hasRole('AUDITOR')")
    @Auditable(action = "ALERT_STATISTICS_VIEW", resource = "SECURITY")
    public ResponseEntity<AlertStatistics> getAlertStatistics() {
        AlertStatistics statistics = notificationService.getAlertStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Trigger manual security scan
     */
    @PostMapping("/scan")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_OFFICER')")
    @Auditable(action = "MANUAL_SECURITY_SCAN", resource = "SECURITY", highRisk = true)
    public ResponseEntity<Map<String, String>> triggerSecurityScan() {
        log.info("Manual security scan triggered");
        
        // Trigger immediate security analysis
        securityMonitoringService.analyzeSecurityTrends();
        
        notificationService.sendSystemNotification(
            "Manual security scan initiated", "INFO");
        
        return ResponseEntity.ok(Map.of(
            "message", "Security scan initiated",
            "status", "SUCCESS"
        ));
    }

    /**
     * Send test security alert
     */
    @PostMapping("/test-alert")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "TEST_ALERT_SEND", resource = "SECURITY", highRisk = true)
    public ResponseEntity<Map<String, String>> sendTestAlert(
            @RequestParam(defaultValue = "MEDIUM") String severity) {
        
        SecurityAlert testAlert = SecurityAlert.builder()
            .alertType("TEST_ALERT")
            .message("This is a test security alert for system validation")
            .severity(severity)
            .username("SYSTEM_TEST")
            .ipAddress("127.0.0.1")
            .timestamp(java.time.LocalDateTime.now())
            .build();
        
        notificationService.sendSecurityAlert(testAlert);
        
        return ResponseEntity.ok(Map.of(
            "message", "Test alert sent successfully",
            "severity", severity,
            "status", "SUCCESS"
        ));
    }

    /**
     * Get security monitoring configuration
     */
    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "SECURITY_CONFIG_VIEW", resource = "SECURITY")
    public ResponseEntity<Map<String, Object>> getSecurityConfig() {
        Map<String, Object> config = Map.of(
            "failedLoginThresholdIp", 10,
            "failedLoginThresholdUser", 5,
            "highRiskThreshold", 20,
            "alertCooldownMinutes", 15,
            "monitoringEnabled", true,
            "realTimeAlertsEnabled", true,
            "scheduledAnalysisInterval", "5 minutes",
            "cleanupInterval", "1 hour"
        );
        
        return ResponseEntity.ok(config);
    }

    /**
     * Get threat intelligence summary
     */
    @GetMapping("/threats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY_OFFICER') or hasRole('AUDITOR')")
    @Auditable(action = "THREAT_INTELLIGENCE_VIEW", resource = "SECURITY")
    public ResponseEntity<Map<String, Object>> getThreatIntelligence() {
        SecurityStatus status = securityMonitoringService.getCurrentSecurityStatus();
        AlertStatistics alertStats = notificationService.getAlertStatistics();
        
        Map<String, Object> threatIntel = Map.of(
            "currentThreatLevel", status.getThreatLevel(),
            "activeThreats", Map.of(
                "ipBasedThreats", status.getActiveIpThreats(),
                "userBasedThreats", status.getActiveUserThreats()
            ),
            "recentActivity", Map.of(
                "totalActivities", status.getTotalActivities(),
                "failedActivities", status.getFailedActivities(),
                "highRiskActivities", status.getHighRiskActivities(),
                "anomalousActivities", status.getAnomalousActivities()
            ),
            "alertSummary", Map.of(
                "criticalAlerts", alertStats.getCriticalAlerts(),
                "highAlerts", alertStats.getHighAlerts(),
                "mediumAlerts", alertStats.getMediumAlerts(),
                "lowAlerts", alertStats.getLowAlerts(),
                "totalAlerts", alertStats.getTotalAlerts()
            ),
            "recommendations", generateSecurityRecommendations(status, alertStats)
        );
        
        return ResponseEntity.ok(threatIntel);
    }

    /**
     * Generate security recommendations based on current status
     */
    private List<String> generateSecurityRecommendations(SecurityStatus status, AlertStatistics alertStats) {
        List<String> recommendations = new java.util.ArrayList<>();
        
        if ("CRITICAL".equals(status.getThreatLevel())) {
            recommendations.add("IMMEDIATE ACTION REQUIRED: Critical threat level detected");
            recommendations.add("Review all recent high-risk activities immediately");
            recommendations.add("Consider implementing additional access controls");
        } else if ("HIGH".equals(status.getThreatLevel())) {
            recommendations.add("Increased monitoring recommended");
            recommendations.add("Review failed login patterns");
            recommendations.add("Verify user access permissions");
        }
        
        if (status.getActiveIpThreats() > 0) {
            recommendations.add("Consider IP-based blocking for suspicious addresses");
        }
        
        if (status.getActiveUserThreats() > 0) {
            recommendations.add("Review user accounts with multiple failed attempts");
        }
        
        if (alertStats.getCriticalAlerts() > 0) {
            recommendations.add("Investigate all critical alerts immediately");
        }
        
        if (status.getAnomalousActivities() > 10) {
            recommendations.add("High number of anomalies detected - review user behavior patterns");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Security status is normal - continue regular monitoring");
        }
        
        return recommendations;
    }
}
