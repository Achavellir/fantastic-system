package com.samap.service;

import com.samap.service.SecurityMonitoringService.SecurityAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Notification service for security alerts and system notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    // In-memory storage for recent alerts (in production, use Redis or database)
    private final ConcurrentLinkedQueue<SecurityAlert> recentAlerts = new ConcurrentLinkedQueue<>();
    private static final int MAX_RECENT_ALERTS = 100;

    /**
     * Send security alert notification
     */
    @Async
    public void sendSecurityAlert(SecurityAlert alert) {
        try {
            // Store alert in memory
            storeAlert(alert);

            // Log the alert
            logAlert(alert);

            // In production, you would integrate with:
            // - Email service (SendGrid, AWS SES)
            // - SMS service (Twilio, AWS SNS)
            // - Slack/Teams webhooks
            // - PagerDuty for critical alerts
            // - SIEM systems

            // For now, we'll simulate different notification channels
            switch (alert.getSeverity()) {
                case "CRITICAL":
                    sendCriticalAlert(alert);
                    break;
                case "HIGH":
                    sendHighPriorityAlert(alert);
                    break;
                case "MEDIUM":
                    sendMediumPriorityAlert(alert);
                    break;
                default:
                    sendLowPriorityAlert(alert);
                    break;
            }

        } catch (Exception e) {
            log.error("Failed to send security alert: {}", e.getMessage(), e);
        }
    }

    /**
     * Store alert in memory (limited to recent alerts)
     */
    private void storeAlert(SecurityAlert alert) {
        recentAlerts.offer(alert);
        
        // Keep only recent alerts
        while (recentAlerts.size() > MAX_RECENT_ALERTS) {
            recentAlerts.poll();
        }
    }

    /**
     * Log security alert
     */
    private void logAlert(SecurityAlert alert) {
        String timestamp = alert.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        log.warn("🚨 SECURITY ALERT [{}] - {} | Type: {} | User: {} | IP: {} | Time: {}", 
            alert.getSeverity(),
            alert.getMessage(),
            alert.getAlertType(),
            alert.getUsername(),
            alert.getIpAddress(),
            timestamp
        );
    }

    /**
     * Send critical alert (highest priority)
     */
    private void sendCriticalAlert(SecurityAlert alert) {
        log.error("🔴 CRITICAL SECURITY ALERT: {}", alert.getMessage());
        
        // In production:
        // - Send immediate email to security team
        // - Send SMS to on-call personnel
        // - Create PagerDuty incident
        // - Post to emergency Slack channel
        // - Trigger automated response procedures
        
        simulateNotification("EMAIL", "security-team@company.com", alert);
        simulateNotification("SMS", "+1-555-SECURITY", alert);
        simulateNotification("PAGERDUTY", "incident-creation", alert);
    }

    /**
     * Send high priority alert
     */
    private void sendHighPriorityAlert(SecurityAlert alert) {
        log.warn("🟠 HIGH PRIORITY SECURITY ALERT: {}", alert.getMessage());
        
        // In production:
        // - Send email to security team
        // - Post to security Slack channel
        // - Update security dashboard
        
        simulateNotification("EMAIL", "security-team@company.com", alert);
        simulateNotification("SLACK", "#security-alerts", alert);
    }

    /**
     * Send medium priority alert
     */
    private void sendMediumPriorityAlert(SecurityAlert alert) {
        log.info("🟡 MEDIUM PRIORITY SECURITY ALERT: {}", alert.getMessage());
        
        // In production:
        // - Send email digest
        // - Post to monitoring channel
        // - Log to SIEM system
        
        simulateNotification("EMAIL_DIGEST", "security-team@company.com", alert);
        simulateNotification("SLACK", "#security-monitoring", alert);
    }

    /**
     * Send low priority alert
     */
    private void sendLowPriorityAlert(SecurityAlert alert) {
        log.debug("🟢 LOW PRIORITY SECURITY ALERT: {}", alert.getMessage());
        
        // In production:
        // - Add to daily digest
        // - Log to audit system
        
        simulateNotification("DAILY_DIGEST", "security-team@company.com", alert);
    }

    /**
     * Simulate notification sending (replace with real implementations)
     */
    private void simulateNotification(String channel, String destination, SecurityAlert alert) {
        log.debug("📧 Sending {} notification to {}: {}", channel, destination, alert.getMessage());
        
        // In production, implement actual notification logic:
        /*
        switch (channel) {
            case "EMAIL":
                emailService.sendSecurityAlert(destination, alert);
                break;
            case "SMS":
                smsService.sendAlert(destination, alert.getMessage());
                break;
            case "SLACK":
                slackService.postMessage(destination, formatSlackMessage(alert));
                break;
            case "PAGERDUTY":
                pagerDutyService.createIncident(alert);
                break;
            // ... other channels
        }
        */
    }

    /**
     * Get recent security alerts
     */
    public List<SecurityAlert> getRecentAlerts(int limit) {
        return recentAlerts.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    /**
     * Get alerts by severity
     */
    public List<SecurityAlert> getAlertsBySeverity(String severity, int limit) {
        return recentAlerts.stream()
                .filter(alert -> severity.equals(alert.getSeverity()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();
    }

    /**
     * Get alert statistics
     */
    public AlertStatistics getAlertStatistics() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        long critical = recentAlerts.stream()
                .filter(alert -> "CRITICAL".equals(alert.getSeverity()))
                .filter(alert -> alert.getTimestamp().isAfter(since))
                .count();
                
        long high = recentAlerts.stream()
                .filter(alert -> "HIGH".equals(alert.getSeverity()))
                .filter(alert -> alert.getTimestamp().isAfter(since))
                .count();
                
        long medium = recentAlerts.stream()
                .filter(alert -> "MEDIUM".equals(alert.getSeverity()))
                .filter(alert -> alert.getTimestamp().isAfter(since))
                .count();
                
        long low = recentAlerts.stream()
                .filter(alert -> "LOW".equals(alert.getSeverity()))
                .filter(alert -> alert.getTimestamp().isAfter(since))
                .count();

        return AlertStatistics.builder()
                .criticalAlerts(critical)
                .highAlerts(high)
                .mediumAlerts(medium)
                .lowAlerts(low)
                .totalAlerts(critical + high + medium + low)
                .period("24 hours")
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Send system notification
     */
    @Async
    public void sendSystemNotification(String message, String level) {
        log.info("📢 SYSTEM NOTIFICATION [{}]: {}", level, message);

        // In production, send to appropriate channels based on level
        simulateNotification("SYSTEM", "admin-team@company.com",
            SecurityAlert.builder()
                .alertType("SYSTEM_NOTIFICATION")
                .message(message)
                .severity(level)
                .username("SYSTEM")
                .ipAddress("SYSTEM")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Send security alert with title and severity
     */
    public void sendSecurityAlert(String title, String message, String severity) {
        SecurityAlert alert = SecurityAlert.builder()
                .alertType("SECURITY_ALERT")
                .message(title + ": " + message)
                .severity(severity)
                .username("SYSTEM")
                .ipAddress("SYSTEM")
                .timestamp(LocalDateTime.now())
                .build();

        sendSecurityAlert(alert);
    }

    /**
     * Send user notification
     */
    public void sendUserNotification(String username, String title, String message) {
        log.info("📧 USER NOTIFICATION to {}: {} - {}", username, title, message);

        // In production, send to user's preferred notification channels
        simulateNotification("USER_EMAIL", username + "@company.com",
            SecurityAlert.builder()
                .alertType("USER_NOTIFICATION")
                .message(title + ": " + message)
                .severity("INFO")
                .username(username)
                .ipAddress("SYSTEM")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    /**
     * Send compliance alert
     */
    public void sendComplianceAlert(String reportType, String message) {
        log.warn("📋 COMPLIANCE ALERT [{}]: {}", reportType, message);

        simulateNotification("COMPLIANCE", "compliance-team@company.com",
            SecurityAlert.builder()
                .alertType("COMPLIANCE_ALERT")
                .message(reportType + ": " + message)
                .severity("MEDIUM")
                .username("SYSTEM")
                .ipAddress("SYSTEM")
                .timestamp(LocalDateTime.now())
                .build()
        );
    }

    // DTO for alert statistics
    public static class AlertStatistics {
        private long criticalAlerts;
        private long highAlerts;
        private long mediumAlerts;
        private long lowAlerts;
        private long totalAlerts;
        private String period;
        private LocalDateTime timestamp;

        public static AlertStatisticsBuilder builder() {
            return new AlertStatisticsBuilder();
        }

        // Getters and setters
        public long getCriticalAlerts() { return criticalAlerts; }
        public void setCriticalAlerts(long criticalAlerts) { this.criticalAlerts = criticalAlerts; }
        public long getHighAlerts() { return highAlerts; }
        public void setHighAlerts(long highAlerts) { this.highAlerts = highAlerts; }
        public long getMediumAlerts() { return mediumAlerts; }
        public void setMediumAlerts(long mediumAlerts) { this.mediumAlerts = mediumAlerts; }
        public long getLowAlerts() { return lowAlerts; }
        public void setLowAlerts(long lowAlerts) { this.lowAlerts = lowAlerts; }
        public long getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(long totalAlerts) { this.totalAlerts = totalAlerts; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public static class AlertStatisticsBuilder {
            private long criticalAlerts;
            private long highAlerts;
            private long mediumAlerts;
            private long lowAlerts;
            private long totalAlerts;
            private String period;
            private LocalDateTime timestamp;

            public AlertStatisticsBuilder criticalAlerts(long criticalAlerts) {
                this.criticalAlerts = criticalAlerts;
                return this;
            }

            public AlertStatisticsBuilder highAlerts(long highAlerts) {
                this.highAlerts = highAlerts;
                return this;
            }

            public AlertStatisticsBuilder mediumAlerts(long mediumAlerts) {
                this.mediumAlerts = mediumAlerts;
                return this;
            }

            public AlertStatisticsBuilder lowAlerts(long lowAlerts) {
                this.lowAlerts = lowAlerts;
                return this;
            }

            public AlertStatisticsBuilder totalAlerts(long totalAlerts) {
                this.totalAlerts = totalAlerts;
                return this;
            }

            public AlertStatisticsBuilder period(String period) {
                this.period = period;
                return this;
            }

            public AlertStatisticsBuilder timestamp(LocalDateTime timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public AlertStatistics build() {
                AlertStatistics stats = new AlertStatistics();
                stats.criticalAlerts = this.criticalAlerts;
                stats.highAlerts = this.highAlerts;
                stats.mediumAlerts = this.mediumAlerts;
                stats.lowAlerts = this.lowAlerts;
                stats.totalAlerts = this.totalAlerts;
                stats.period = this.period;
                stats.timestamp = this.timestamp;
                return stats;
            }
        }
    }
}
