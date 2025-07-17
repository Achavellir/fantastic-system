package com.samap.service;

import com.samap.model.AuditLog;
import com.samap.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Real-time security monitoring service for threat detection and alerting
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityMonitoringService {

    private final AuditLogRepository auditLogRepository;
    private final NotificationService notificationService;

    // In-memory threat tracking
    private final Map<String, AtomicInteger> failedLoginsByIp = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failedLoginsByUser = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAlertTime = new ConcurrentHashMap<>();

    // Thresholds
    private static final int FAILED_LOGIN_THRESHOLD_IP = 10;
    private static final int FAILED_LOGIN_THRESHOLD_USER = 5;
    private static final int HIGH_RISK_THRESHOLD = 20;
    private static final int ALERT_COOLDOWN_MINUTES = 15;

    /**
     * Monitor failed login attempts in real-time
     */
    @Async
    public void monitorFailedLogin(String username, String ipAddress) {
        log.debug("Monitoring failed login: user={}, ip={}", username, ipAddress);

        // Track by IP
        AtomicInteger ipFailures = failedLoginsByIp.computeIfAbsent(ipAddress, k -> new AtomicInteger(0));
        int ipCount = ipFailures.incrementAndGet();

        // Track by user
        AtomicInteger userFailures = failedLoginsByUser.computeIfAbsent(username, k -> new AtomicInteger(0));
        int userCount = userFailures.incrementAndGet();

        // Check for IP-based attack
        if (ipCount >= FAILED_LOGIN_THRESHOLD_IP) {
            triggerSecurityAlert("BRUTE_FORCE_IP", 
                String.format("Brute force attack detected from IP: %s (%d failed attempts)", ipAddress, ipCount),
                ipAddress, username, "HIGH");
        }

        // Check for user-based attack
        if (userCount >= FAILED_LOGIN_THRESHOLD_USER) {
            triggerSecurityAlert("BRUTE_FORCE_USER", 
                String.format("Multiple failed login attempts for user: %s (%d attempts)", username, userCount),
                ipAddress, username, "MEDIUM");
        }
    }

    /**
     * Monitor high-risk activities
     */
    @Async
    public void monitorHighRiskActivity(AuditLog auditLog) {
        if (auditLog.getRiskScore() != null && auditLog.getRiskScore() > 0.7) {
            triggerSecurityAlert("HIGH_RISK_ACTIVITY",
                String.format("High-risk activity detected: %s by %s (Risk Score: %.2f)", 
                    auditLog.getAction(), auditLog.getUsername(), auditLog.getRiskScore()),
                auditLog.getIpAddress(), auditLog.getUsername(), "HIGH");
        }
    }

    /**
     * Monitor anomalous activities
     */
    @Async
    public void monitorAnomalousActivity(AuditLog auditLog) {
        if (Boolean.TRUE.equals(auditLog.getIsAnomaly())) {
            triggerSecurityAlert("ANOMALY_DETECTED",
                String.format("Anomalous activity detected: %s by %s - %s", 
                    auditLog.getAction(), auditLog.getUsername(), auditLog.getAnomalyReasons()),
                auditLog.getIpAddress(), auditLog.getUsername(), "MEDIUM");
        }
    }

    /**
     * Trigger security alert with cooldown mechanism
     */
    private void triggerSecurityAlert(String alertType, String message, String ipAddress, 
                                    String username, String severity) {
        String alertKey = alertType + ":" + ipAddress + ":" + username;
        LocalDateTime now = LocalDateTime.now();
        
        // Check cooldown period
        LocalDateTime lastAlert = lastAlertTime.get(alertKey);
        if (lastAlert != null && lastAlert.plusMinutes(ALERT_COOLDOWN_MINUTES).isAfter(now)) {
            log.debug("Alert suppressed due to cooldown: {}", alertKey);
            return;
        }

        // Update last alert time
        lastAlertTime.put(alertKey, now);

        // Create security alert
        SecurityAlert alert = SecurityAlert.builder()
            .alertType(alertType)
            .message(message)
            .severity(severity)
            .ipAddress(ipAddress)
            .username(username)
            .timestamp(now)
            .build();

        // Log the alert
        log.warn("SECURITY ALERT [{}]: {} - IP: {}, User: {}", severity, message, ipAddress, username);

        // Send notification
        notificationService.sendSecurityAlert(alert);
    }

    /**
     * Scheduled task to analyze security trends
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void analyzeSecurityTrends() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        
        try {
            // Get recent high-risk activities
            long highRiskCount = auditLogRepository.findHighRiskActivities(
                org.springframework.data.domain.PageRequest.of(0, 100)).getTotalElements();

            if (highRiskCount > HIGH_RISK_THRESHOLD) {
                triggerSecurityAlert("HIGH_RISK_TREND",
                    String.format("High number of risk activities in last 5 minutes: %d", highRiskCount),
                    "SYSTEM", "SYSTEM", "HIGH");
            }

            // Get recent anomalies
            long anomalyCount = auditLogRepository.findByIsAnomalyTrueOrderByTimestampDesc(
                org.springframework.data.domain.PageRequest.of(0, 100)).getTotalElements();

            if (anomalyCount > 10) {
                triggerSecurityAlert("ANOMALY_TREND",
                    String.format("High number of anomalies detected in last 5 minutes: %d", anomalyCount),
                    "SYSTEM", "SYSTEM", "MEDIUM");
            }

            log.debug("Security trend analysis completed - High Risk: {}, Anomalies: {}", 
                highRiskCount, anomalyCount);

        } catch (Exception e) {
            log.error("Error during security trend analysis: {}", e.getMessage(), e);
        }
    }

    /**
     * Scheduled task to clean up tracking maps
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupTrackingMaps() {
        log.debug("Cleaning up security monitoring tracking maps");
        
        // Reset failed login counters
        failedLoginsByIp.clear();
        failedLoginsByUser.clear();
        
        // Clean old alert timestamps
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        lastAlertTime.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        
        log.debug("Security monitoring cleanup completed");
    }

    /**
     * Get current security status
     */
    public SecurityStatus getCurrentSecurityStatus() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        
        try {
            // Get activity statistics
            Object[] stats = auditLogRepository.getActivityStatistics(since);
            
            long totalActivities = ((Number) stats[0]).longValue();
            long failedActivities = ((Number) stats[2]).longValue();
            long highRiskActivities = ((Number) stats[3]).longValue();
            long anomalousActivities = ((Number) stats[4]).longValue();

            // Calculate threat level
            String threatLevel = calculateThreatLevel(failedActivities, highRiskActivities, anomalousActivities);

            return SecurityStatus.builder()
                .threatLevel(threatLevel)
                .totalActivities(totalActivities)
                .failedActivities(failedActivities)
                .highRiskActivities(highRiskActivities)
                .anomalousActivities(anomalousActivities)
                .activeIpThreats(failedLoginsByIp.size())
                .activeUserThreats(failedLoginsByUser.size())
                .timestamp(LocalDateTime.now())
                .build();

        } catch (Exception e) {
            log.error("Error getting security status: {}", e.getMessage(), e);
            return SecurityStatus.builder()
                .threatLevel("UNKNOWN")
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    /**
     * Calculate overall threat level
     */
    private String calculateThreatLevel(long failedActivities, long highRiskActivities, long anomalousActivities) {
        if (highRiskActivities > 50 || anomalousActivities > 20) {
            return "CRITICAL";
        } else if (highRiskActivities > 20 || anomalousActivities > 10 || failedActivities > 100) {
            return "HIGH";
        } else if (highRiskActivities > 5 || anomalousActivities > 3 || failedActivities > 20) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    // DTOs
    public static class SecurityAlert {
        private String alertType;
        private String message;
        private String severity;
        private String ipAddress;
        private String username;
        private LocalDateTime timestamp;

        public static SecurityAlertBuilder builder() {
            return new SecurityAlertBuilder();
        }

        // Getters and setters
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public static class SecurityAlertBuilder {
            private String alertType;
            private String message;
            private String severity;
            private String ipAddress;
            private String username;
            private LocalDateTime timestamp;

            public SecurityAlertBuilder alertType(String alertType) {
                this.alertType = alertType;
                return this;
            }

            public SecurityAlertBuilder message(String message) {
                this.message = message;
                return this;
            }

            public SecurityAlertBuilder severity(String severity) {
                this.severity = severity;
                return this;
            }

            public SecurityAlertBuilder ipAddress(String ipAddress) {
                this.ipAddress = ipAddress;
                return this;
            }

            public SecurityAlertBuilder username(String username) {
                this.username = username;
                return this;
            }

            public SecurityAlertBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public SecurityAlert build() {
                SecurityAlert alert = new SecurityAlert();
                alert.alertType = this.alertType;
                alert.message = this.message;
                alert.severity = this.severity;
                alert.ipAddress = this.ipAddress;
                alert.username = this.username;
                alert.timestamp = this.timestamp;
                return alert;
            }
        }
    }

    public static class SecurityStatus {
        private String threatLevel;
        private long totalActivities;
        private long failedActivities;
        private long highRiskActivities;
        private long anomalousActivities;
        private int activeIpThreats;
        private int activeUserThreats;
        private LocalDateTime timestamp;

        public static SecurityStatusBuilder builder() {
            return new SecurityStatusBuilder();
        }

        // Getters and setters
        public String getThreatLevel() { return threatLevel; }
        public void setThreatLevel(String threatLevel) { this.threatLevel = threatLevel; }
        public long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }
        public long getFailedActivities() { return failedActivities; }
        public void setFailedActivities(long failedActivities) { this.failedActivities = failedActivities; }
        public long getHighRiskActivities() { return highRiskActivities; }
        public void setHighRiskActivities(long highRiskActivities) { this.highRiskActivities = highRiskActivities; }
        public long getAnomalousActivities() { return anomalousActivities; }
        public void setAnomalousActivities(long anomalousActivities) { this.anomalousActivities = anomalousActivities; }
        public int getActiveIpThreats() { return activeIpThreats; }
        public void setActiveIpThreats(int activeIpThreats) { this.activeIpThreats = activeIpThreats; }
        public int getActiveUserThreats() { return activeUserThreats; }
        public void setActiveUserThreats(int activeUserThreats) { this.activeUserThreats = activeUserThreats; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public static class SecurityStatusBuilder {
            private String threatLevel;
            private long totalActivities;
            private long failedActivities;
            private long highRiskActivities;
            private long anomalousActivities;
            private int activeIpThreats;
            private int activeUserThreats;
            private LocalDateTime timestamp;

            public SecurityStatusBuilder threatLevel(String threatLevel) {
                this.threatLevel = threatLevel;
                return this;
            }

            public SecurityStatusBuilder totalActivities(long totalActivities) {
                this.totalActivities = totalActivities;
                return this;
            }

            public SecurityStatusBuilder failedActivities(long failedActivities) {
                this.failedActivities = failedActivities;
                return this;
            }

            public SecurityStatusBuilder highRiskActivities(long highRiskActivities) {
                this.highRiskActivities = highRiskActivities;
                return this;
            }

            public SecurityStatusBuilder anomalousActivities(long anomalousActivities) {
                this.anomalousActivities = anomalousActivities;
                return this;
            }

            public SecurityStatusBuilder activeIpThreats(int activeIpThreats) {
                this.activeIpThreats = activeIpThreats;
                return this;
            }

            public SecurityStatusBuilder activeUserThreats(int activeUserThreats) {
                this.activeUserThreats = activeUserThreats;
                return this;
            }

            public SecurityStatusBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public SecurityStatus build() {
                SecurityStatus status = new SecurityStatus();
                status.threatLevel = this.threatLevel;
                status.totalActivities = this.totalActivities;
                status.failedActivities = this.failedActivities;
                status.highRiskActivities = this.highRiskActivities;
                status.anomalousActivities = this.anomalousActivities;
                status.activeIpThreats = this.activeIpThreats;
                status.activeUserThreats = this.activeUserThreats;
                status.timestamp = this.timestamp;
                return status;
            }
        }
    }
}
