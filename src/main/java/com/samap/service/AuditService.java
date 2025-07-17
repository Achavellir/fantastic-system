package com.samap.service;

import com.samap.model.AuditLog;
import com.samap.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Audit service for comprehensive security logging and monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final RiskAssessmentService riskAssessmentService;

    /**
     * Log security event asynchronously
     */
    @Async
    @Transactional
    public void logSecurityEvent(String username, String action, String details, 
                               String ipAddress, String userAgent, AuditLog.ActionStatus status) {
        logSecurityEvent(username, action, details, ipAddress, userAgent, status, 0.0);
    }

    /**
     * Log security event with risk score
     */
    @Async
    @Transactional
    public void logSecurityEvent(String username, String action, String details, 
                               String ipAddress, String userAgent, AuditLog.ActionStatus status, 
                               double riskScore) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUsername(username);
            auditLog.setAction(action);
            auditLog.setDetails(details);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setStatus(status);
            auditLog.setRiskScore(riskScore);
            auditLog.setRiskLevelFromScore();
            auditLog.setCorrelationId(UUID.randomUUID().toString());
            auditLog.setTimestamp(LocalDateTime.now());

            // Perform anomaly detection
            boolean isAnomaly = riskAssessmentService.isAnomalousActivity(auditLog);
            auditLog.setIsAnomaly(isAnomaly);

            if (isAnomaly) {
                auditLog.setAnomalyReasons(riskAssessmentService.getAnomalyReasons(auditLog));
            }

            auditLogRepository.save(auditLog);

            // Log high-risk activities
            if (auditLog.isHighRisk()) {
                log.warn("High-risk activity detected: {} by {} from {} - Risk Score: {}", 
                    action, username, ipAddress, riskScore);
            }

        } catch (Exception e) {
            log.error("Failed to log audit event: {}", e.getMessage(), e);
        }
    }

    /**
     * Log API access
     */
    @Async
    @Transactional
    public void logApiAccess(String username, String endpoint, String httpMethod, 
                           String requestParams, Integer responseStatus, Long executionTime,
                           String ipAddress, String userAgent, String sessionId) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUsername(username);
            auditLog.setAction(AuditLog.ACTION_DATA_ACCESS);
            auditLog.setEndpoint(endpoint);
            auditLog.setHttpMethod(httpMethod);
            auditLog.setRequestParams(requestParams);
            auditLog.setResponseStatus(responseStatus);
            auditLog.setExecutionTimeMs(executionTime);
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setSessionId(sessionId);
            auditLog.setStatus(responseStatus >= 200 && responseStatus < 300 ? 
                AuditLog.ActionStatus.SUCCESS : AuditLog.ActionStatus.FAILURE);
            auditLog.setCorrelationId(UUID.randomUUID().toString());
            auditLog.setTimestamp(LocalDateTime.now());

            // Calculate risk score for API access
            double riskScore = riskAssessmentService.calculateApiAccessRiskScore(auditLog);
            auditLog.setRiskScore(riskScore);
            auditLog.setRiskLevelFromScore();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Failed to log API access: {}", e.getMessage(), e);
        }
    }

    /**
     * Get audit logs with pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs by username
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable);
    }

    /**
     * Get audit logs by action
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }

    /**
     * Get high-risk activities
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getHighRiskActivities(Pageable pageable) {
        return auditLogRepository.findHighRiskActivities(pageable);
    }

    /**
     * Get anomalous activities
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAnomalousActivities(Pageable pageable) {
        return auditLogRepository.findByIsAnomalyTrueOrderByTimestampDesc(pageable);
    }

    /**
     * Get failed login attempts
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getFailedLoginAttempts(Pageable pageable) {
        return auditLogRepository.findFailedLoginAttempts(pageable);
    }

    /**
     * Search audit logs
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(String searchTerm, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(searchTerm, pageable);
    }

    /**
     * Get audit logs within date range
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, 
                                                 Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    /**
     * Get activity statistics
     */
    @Transactional(readOnly = true)
    public ActivityStatistics getActivityStatistics(LocalDateTime since) {
        Object[] stats = auditLogRepository.getActivityStatistics(since);
        
        return ActivityStatistics.builder()
                .totalActivities(((Number) stats[0]).longValue())
                .successfulActivities(((Number) stats[1]).longValue())
                .failedActivities(((Number) stats[2]).longValue())
                .highRiskActivities(((Number) stats[3]).longValue())
                .anomalousActivities(((Number) stats[4]).longValue())
                .build();
    }

    /**
     * Get top active users
     */
    @Transactional(readOnly = true)
    public List<UserActivity> getTopActiveUsers(LocalDateTime since, Pageable pageable) {
        List<Object[]> results = auditLogRepository.getTopActiveUsers(since, pageable);
        
        return results.stream()
                .map(result -> new UserActivity(
                    (String) result[0], 
                    ((Number) result[1]).longValue()
                ))
                .toList();
    }

    /**
     * Get activity trends by hour
     */
    @Transactional(readOnly = true)
    public List<HourlyActivity> getActivityTrendsByHour(LocalDateTime since) {
        List<Object[]> results = auditLogRepository.getActivityTrendsByHour(since);
        
        return results.stream()
                .map(result -> new HourlyActivity(
                    ((Number) result[0]).intValue(),
                    ((Number) result[1]).longValue()
                ))
                .toList();
    }

    /**
     * Clean up old audit logs
     */
    @Transactional
    public void cleanupOldAuditLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        auditLogRepository.deleteByTimestampBefore(cutoffDate);
        log.info("Cleaned up audit logs older than {} days", retentionDays);
    }

    // DTOs
    public static class ActivityStatistics {
        private long totalActivities;
        private long successfulActivities;
        private long failedActivities;
        private long highRiskActivities;
        private long anomalousActivities;

        public static ActivityStatisticsBuilder builder() {
            return new ActivityStatisticsBuilder();
        }

        // Getters and setters
        public long getTotalActivities() { return totalActivities; }
        public void setTotalActivities(long totalActivities) { this.totalActivities = totalActivities; }
        public long getSuccessfulActivities() { return successfulActivities; }
        public void setSuccessfulActivities(long successfulActivities) { this.successfulActivities = successfulActivities; }
        public long getFailedActivities() { return failedActivities; }
        public void setFailedActivities(long failedActivities) { this.failedActivities = failedActivities; }
        public long getHighRiskActivities() { return highRiskActivities; }
        public void setHighRiskActivities(long highRiskActivities) { this.highRiskActivities = highRiskActivities; }
        public long getAnomalousActivities() { return anomalousActivities; }
        public void setAnomalousActivities(long anomalousActivities) { this.anomalousActivities = anomalousActivities; }

        public static class ActivityStatisticsBuilder {
            private long totalActivities;
            private long successfulActivities;
            private long failedActivities;
            private long highRiskActivities;
            private long anomalousActivities;

            public ActivityStatisticsBuilder totalActivities(long totalActivities) {
                this.totalActivities = totalActivities;
                return this;
            }

            public ActivityStatisticsBuilder successfulActivities(long successfulActivities) {
                this.successfulActivities = successfulActivities;
                return this;
            }

            public ActivityStatisticsBuilder failedActivities(long failedActivities) {
                this.failedActivities = failedActivities;
                return this;
            }

            public ActivityStatisticsBuilder highRiskActivities(long highRiskActivities) {
                this.highRiskActivities = highRiskActivities;
                return this;
            }

            public ActivityStatisticsBuilder anomalousActivities(long anomalousActivities) {
                this.anomalousActivities = anomalousActivities;
                return this;
            }

            public ActivityStatistics build() {
                ActivityStatistics stats = new ActivityStatistics();
                stats.totalActivities = this.totalActivities;
                stats.successfulActivities = this.successfulActivities;
                stats.failedActivities = this.failedActivities;
                stats.highRiskActivities = this.highRiskActivities;
                stats.anomalousActivities = this.anomalousActivities;
                return stats;
            }
        }
    }

    public static class UserActivity {
        private String username;
        private long activityCount;

        public UserActivity(String username, long activityCount) {
            this.username = username;
            this.activityCount = activityCount;
        }

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public long getActivityCount() { return activityCount; }
        public void setActivityCount(long activityCount) { this.activityCount = activityCount; }
    }

    public static class HourlyActivity {
        private int hour;
        private long activityCount;

        public HourlyActivity(int hour, long activityCount) {
            this.hour = hour;
            this.activityCount = activityCount;
        }

        // Getters and setters
        public int getHour() { return hour; }
        public void setHour(int hour) { this.hour = hour; }
        public long getActivityCount() { return activityCount; }
        public void setActivityCount(long activityCount) { this.activityCount = activityCount; }
    }
}
