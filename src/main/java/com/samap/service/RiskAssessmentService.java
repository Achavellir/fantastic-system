package com.samap.service;

import com.samap.model.AuditLog;
import com.samap.model.User;
import com.samap.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Risk assessment service for calculating security risk scores and detecting anomalies
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RiskAssessmentService {

    private final AuditLogRepository auditLogRepository;

    // Risk scoring weights
    private static final double FAILED_LOGIN_WEIGHT = 0.3;
    private static final double TIME_ANOMALY_WEIGHT = 0.2;
    private static final double LOCATION_ANOMALY_WEIGHT = 0.25;
    private static final double FREQUENCY_ANOMALY_WEIGHT = 0.15;
    private static final double DEVICE_ANOMALY_WEIGHT = 0.1;

    // Thresholds
    private static final int FAILED_LOGIN_THRESHOLD = 3;
    private static final int FREQUENCY_THRESHOLD_PER_HOUR = 50;
    private static final LocalTime NORMAL_START_TIME = LocalTime.of(8, 0);
    private static final LocalTime NORMAL_END_TIME = LocalTime.of(18, 0);

    /**
     * Calculate login risk score
     */
    public double calculateLoginRiskScore(User user, String ipAddress, String userAgent) {
        double riskScore = 0.0;
        List<String> riskFactors = new ArrayList<>();

        // Check failed login attempts
        long recentFailedAttempts = auditLogRepository.countFailedLoginsByUserSince(
            user.getUsername(), LocalDateTime.now().minusHours(1));
        
        if (recentFailedAttempts >= FAILED_LOGIN_THRESHOLD) {
            double failedLoginRisk = Math.min(recentFailedAttempts / 10.0, 1.0);
            riskScore += failedLoginRisk * FAILED_LOGIN_WEIGHT;
            riskFactors.add("Recent failed login attempts: " + recentFailedAttempts);
        }

        // Check time-based anomaly
        LocalTime currentTime = LocalDateTime.now().toLocalTime();
        if (currentTime.isBefore(NORMAL_START_TIME) || currentTime.isAfter(NORMAL_END_TIME)) {
            riskScore += 0.4 * TIME_ANOMALY_WEIGHT;
            riskFactors.add("Login outside normal hours: " + currentTime);
        }

        // Check location anomaly (IP-based)
        if (isNewLocation(user, ipAddress)) {
            riskScore += 0.6 * LOCATION_ANOMALY_WEIGHT;
            riskFactors.add("Login from new location: " + ipAddress);
        }

        // Check login frequency
        long recentLogins = auditLogRepository.countFailedLoginsByUserSince(
            user.getUsername(), LocalDateTime.now().minusHours(1));
        
        if (recentLogins > FREQUENCY_THRESHOLD_PER_HOUR) {
            double frequencyRisk = Math.min(recentLogins / 100.0, 1.0);
            riskScore += frequencyRisk * FREQUENCY_ANOMALY_WEIGHT;
            riskFactors.add("High login frequency: " + recentLogins + " in last hour");
        }

        // Check device anomaly (User-Agent based)
        if (isNewDevice(user, userAgent)) {
            riskScore += 0.5 * DEVICE_ANOMALY_WEIGHT;
            riskFactors.add("Login from new device");
        }

        return Math.min(riskScore, 1.0); // Cap at 1.0
    }

    /**
     * Calculate API access risk score
     */
    public double calculateApiAccessRiskScore(AuditLog auditLog) {
        double riskScore = 0.0;

        // Check for sensitive endpoints
        if (isSensitiveEndpoint(auditLog.getEndpoint())) {
            riskScore += 0.3;
        }

        // Check for failed requests
        if (auditLog.getResponseStatus() != null && auditLog.getResponseStatus() >= 400) {
            riskScore += 0.2;
        }

        // Check execution time (potential DoS or inefficient queries)
        if (auditLog.getExecutionTimeMs() != null && auditLog.getExecutionTimeMs() > 5000) {
            riskScore += 0.2;
        }

        // Check for unusual request patterns
        long recentRequests = countRecentRequestsByUser(auditLog.getUsername(), 
            auditLog.getIpAddress(), LocalDateTime.now().minusMinutes(5));
        
        if (recentRequests > 100) {
            riskScore += 0.3;
        }

        return Math.min(riskScore, 1.0);
    }

    /**
     * Check if activity is anomalous
     */
    public boolean isAnomalousActivity(AuditLog auditLog) {
        // High risk score indicates anomaly
        if (auditLog.getRiskScore() != null && auditLog.getRiskScore() > 0.7) {
            return true;
        }

        // Multiple failed attempts from same IP
        if (AuditLog.ACTION_LOGIN_FAILED.equals(auditLog.getAction())) {
            long failedAttemptsFromIp = auditLogRepository.countFailedLoginsByIpSince(
                auditLog.getIpAddress(), LocalDateTime.now().minusMinutes(10));
            
            if (failedAttemptsFromIp > 5) {
                return true;
            }
        }

        // Unusual time patterns
        LocalTime currentTime = auditLog.getTimestamp().toLocalTime();
        if (currentTime.isBefore(LocalTime.of(2, 0)) || currentTime.isAfter(LocalTime.of(23, 0))) {
            return true;
        }

        return false;
    }

    /**
     * Get anomaly reasons
     */
    public String getAnomalyReasons(AuditLog auditLog) {
        List<String> reasons = new ArrayList<>();

        if (auditLog.getRiskScore() != null && auditLog.getRiskScore() > 0.7) {
            reasons.add("High risk score: " + String.format("%.2f", auditLog.getRiskScore()));
        }

        if (AuditLog.ACTION_LOGIN_FAILED.equals(auditLog.getAction())) {
            long failedAttemptsFromIp = auditLogRepository.countFailedLoginsByIpSince(
                auditLog.getIpAddress(), LocalDateTime.now().minusMinutes(10));
            
            if (failedAttemptsFromIp > 5) {
                reasons.add("Multiple failed attempts from IP: " + failedAttemptsFromIp);
            }
        }

        LocalTime currentTime = auditLog.getTimestamp().toLocalTime();
        if (currentTime.isBefore(LocalTime.of(2, 0)) || currentTime.isAfter(LocalTime.of(23, 0))) {
            reasons.add("Activity during unusual hours: " + currentTime);
        }

        return String.join("; ", reasons);
    }

    /**
     * Check if IP address is from a new location for user
     */
    private boolean isNewLocation(User user, String ipAddress) {
        // Simple implementation - in production, use GeoIP service
        List<AuditLog> recentLogins = auditLogRepository.findFailedLoginAttemptsByUsername(user.getUsername());
        
        return recentLogins.stream()
                .limit(10) // Check last 10 logins
                .noneMatch(log -> ipAddress.equals(log.getIpAddress()));
    }

    /**
     * Check if user agent indicates a new device
     */
    private boolean isNewDevice(User user, String userAgent) {
        if (userAgent == null) return false;
        
        // Simple implementation - in production, parse user agent properly
        List<AuditLog> recentLogins = auditLogRepository.findFailedLoginAttemptsByUsername(user.getUsername());
        
        return recentLogins.stream()
                .limit(5) // Check last 5 logins
                .noneMatch(log -> userAgent.equals(log.getUserAgent()));
    }

    /**
     * Check if endpoint is sensitive
     */
    private boolean isSensitiveEndpoint(String endpoint) {
        if (endpoint == null) return false;
        
        String[] sensitivePatterns = {
            "/api/admin/",
            "/api/users/",
            "/api/roles/",
            "/api/audit/export",
            "/api/system/"
        };
        
        for (String pattern : sensitivePatterns) {
            if (endpoint.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Count recent requests by user and IP
     */
    private long countRecentRequestsByUser(String username, String ipAddress, LocalDateTime since) {
        // This would need a custom query in a real implementation
        return 0; // Placeholder
    }

    /**
     * Generate risk assessment report
     */
    public RiskAssessmentReport generateRiskReport(String username, LocalDateTime since) {
        List<AuditLog> userActivities = auditLogRepository.findByUsernameOrderByTimestampDesc(
            username, org.springframework.data.domain.Pageable.unpaged()).getContent();

        long totalActivities = userActivities.size();
        long highRiskActivities = userActivities.stream()
                .mapToLong(log -> log.isHighRisk() ? 1 : 0)
                .sum();
        
        long anomalousActivities = userActivities.stream()
                .mapToLong(log -> Boolean.TRUE.equals(log.getIsAnomaly()) ? 1 : 0)
                .sum();

        double averageRiskScore = userActivities.stream()
                .filter(log -> log.getRiskScore() != null)
                .mapToDouble(AuditLog::getRiskScore)
                .average()
                .orElse(0.0);

        return RiskAssessmentReport.builder()
                .username(username)
                .assessmentPeriod(since)
                .totalActivities(totalActivities)
                .highRiskActivities(highRiskActivities)
                .anomalousActivities(anomalousActivities)
                .averageRiskScore(averageRiskScore)
                .riskLevel(calculateOverallRiskLevel(averageRiskScore, highRiskActivities, totalActivities))
                .build();
    }

    /**
     * Calculate overall risk level
     */
    private AuditLog.RiskLevel calculateOverallRiskLevel(double averageRiskScore, 
                                                        long highRiskActivities, 
                                                        long totalActivities) {
        if (averageRiskScore > 0.8 || (totalActivities > 0 && (double) highRiskActivities / totalActivities > 0.3)) {
            return AuditLog.RiskLevel.CRITICAL;
        } else if (averageRiskScore > 0.6 || (totalActivities > 0 && (double) highRiskActivities / totalActivities > 0.2)) {
            return AuditLog.RiskLevel.HIGH;
        } else if (averageRiskScore > 0.3 || (totalActivities > 0 && (double) highRiskActivities / totalActivities > 0.1)) {
            return AuditLog.RiskLevel.MEDIUM;
        } else {
            return AuditLog.RiskLevel.LOW;
        }
    }

    // DTO
    public static class RiskAssessmentReport {
        private String username;
        private LocalDateTime assessmentPeriod;
        private long totalActivities;
        private long highRiskActivities;
        private long anomalousActivities;
        private double averageRiskScore;
        private AuditLog.RiskLevel riskLevel;

        public static RiskAssessmentReportBuilder builder() {
            return new RiskAssessmentReportBuilder();
        }

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public LocalDateTime getAssessmentPeriod() { return assessmentPeriod; }
        public void setAssessmentPeriod(LocalDateTime assessmentPeriod) { this.assessmentPeriod = assessmentPeriod; }
        public long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }
        public long getHighRiskActivities() { return highRiskActivities; }
        public void setHighRiskActivities(long highRiskActivities) { this.highRiskActivities = highRiskActivities; }
        public long getAnomalousActivities() { return anomalousActivities; }
        public void setAnomalousActivities(long anomalousActivities) { this.anomalousActivities = anomalousActivities; }
        public double getAverageRiskScore() { return averageRiskScore; }
        public void setAverageRiskScore(double averageRiskScore) { this.averageRiskScore = averageRiskScore; }
        public AuditLog.RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(AuditLog.RiskLevel riskLevel) { this.riskLevel = riskLevel; }

        public static class RiskAssessmentReportBuilder {
            private String username;
            private LocalDateTime assessmentPeriod;
            private long totalActivities;
            private long highRiskActivities;
            private long anomalousActivities;
            private double averageRiskScore;
            private AuditLog.RiskLevel riskLevel;

            public RiskAssessmentReportBuilder username(String username) {
                this.username = username;
                return this;
            }

            public RiskAssessmentReportBuilder assessmentPeriod(LocalDateTime assessmentPeriod) {
                this.assessmentPeriod = assessmentPeriod;
                return this;
            }

            public RiskAssessmentReportBuilder totalActivities(long totalActivities) {
                this.totalActivities = totalActivities;
                return this;
            }

            public RiskAssessmentReportBuilder highRiskActivities(long highRiskActivities) {
                this.highRiskActivities = highRiskActivities;
                return this;
            }

            public RiskAssessmentReportBuilder anomalousActivities(long anomalousActivities) {
                this.anomalousActivities = anomalousActivities;
                return this;
            }

            public RiskAssessmentReportBuilder averageRiskScore(double averageRiskScore) {
                this.averageRiskScore = averageRiskScore;
                return this;
            }

            public RiskAssessmentReportBuilder riskLevel(AuditLog.RiskLevel riskLevel) {
                this.riskLevel = riskLevel;
                return this;
            }

            public RiskAssessmentReport build() {
                RiskAssessmentReport report = new RiskAssessmentReport();
                report.username = this.username;
                report.assessmentPeriod = this.assessmentPeriod;
                report.totalActivities = this.totalActivities;
                report.highRiskActivities = this.highRiskActivities;
                report.anomalousActivities = this.anomalousActivities;
                report.averageRiskScore = this.averageRiskScore;
                report.riskLevel = this.riskLevel;
                return report;
            }
        }
    }
}
