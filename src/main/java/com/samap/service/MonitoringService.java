package com.samap.service;

import com.samap.config.MetricsConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Comprehensive monitoring service for system health and metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final MetricsConfig metricsConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter securityAlertsCounter;
    private final Counter anomalyDetectionCounter;
    private final Counter userCreationCounter;
    private final Counter accountLockoutCounter;
    private final Counter rateLimitExceededCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter auditLogCounter;
    private final Counter complianceReportCounter;
    private final Timer loginDurationTimer;
    private final Timer apiRequestTimer;
    private final Timer databaseQueryTimer;
    private final Timer auditLogProcessingTimer;
    private final Timer riskAssessmentTimer;

    /**
     * Record successful login
     */
    public void recordLoginSuccess(String username, long durationMs) {
        loginSuccessCounter.increment();
        loginDurationTimer.record(durationMs, TimeUnit.MILLISECONDS);
        metricsConfig.incrementActiveUsers();
        
        // Store in Redis for real-time monitoring
        String key = "login:success:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, username, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        
        log.debug("Login success recorded for user: {} in {}ms", username, durationMs);
    }

    /**
     * Record failed login attempt
     */
    public void recordLoginFailure(String username, String reason) {
        loginFailureCounter.increment();
        metricsConfig.incrementFailedLogins();
        
        // Store in Redis for monitoring
        String key = "login:failure:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, username, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        
        log.warn("Login failure recorded for user: {} - reason: {}", username, reason);
    }

    /**
     * Record security alert
     */
    public void recordSecurityAlert(String alertType, String details) {
        securityAlertsCounter.increment();
        
        String key = "security:alerts:" + LocalDateTime.now().toLocalDate();
        Map<String, Object> alertData = new HashMap<>();
        alertData.put("type", alertType);
        alertData.put("details", details);
        alertData.put("timestamp", LocalDateTime.now());
        
        redisTemplate.opsForList().leftPush(key, alertData);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        log.warn("Security alert recorded: {} - {}", alertType, details);
    }

    /**
     * Record anomaly detection
     */
    public void recordAnomalyDetection(String username, String anomalyType, double riskScore) {
        anomalyDetectionCounter.increment();
        metricsConfig.incrementHighRiskActivities();
        
        String key = "anomaly:detection:" + LocalDateTime.now().toLocalDate();
        Map<String, Object> anomalyData = new HashMap<>();
        anomalyData.put("username", username);
        anomalyData.put("type", anomalyType);
        anomalyData.put("riskScore", riskScore);
        anomalyData.put("timestamp", LocalDateTime.now());
        
        redisTemplate.opsForList().leftPush(key, anomalyData);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        log.warn("Anomaly detected for user: {} - type: {} - risk score: {}", 
                username, anomalyType, riskScore);
    }

    /**
     * Record user creation
     */
    public void recordUserCreation(String createdBy) {
        userCreationCounter.increment();
        
        String key = "user:creation:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, createdBy, 1);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        log.info("User creation recorded by: {}", createdBy);
    }

    /**
     * Record account lockout
     */
    public void recordAccountLockout(String username, String reason) {
        accountLockoutCounter.increment();
        
        String key = "account:lockout:" + LocalDateTime.now().toLocalDate();
        Map<String, Object> lockoutData = new HashMap<>();
        lockoutData.put("username", username);
        lockoutData.put("reason", reason);
        lockoutData.put("timestamp", LocalDateTime.now());
        
        redisTemplate.opsForList().leftPush(key, lockoutData);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        log.warn("Account lockout recorded for user: {} - reason: {}", username, reason);
    }

    /**
     * Record API request
     */
    public void recordApiRequest(String endpoint, long durationMs, int statusCode) {
        apiRequestTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        String key = "api:requests:" + LocalDateTime.now().toLocalDate();
        String field = endpoint + ":" + statusCode;
        redisTemplate.opsForHash().increment(key, field, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        
        log.debug("API request recorded: {} - {}ms - status: {}", endpoint, durationMs, statusCode);
    }

    /**
     * Record rate limit exceeded
     */
    public void recordRateLimitExceeded(String clientId, String endpoint) {
        rateLimitExceededCounter.increment();
        
        String key = "rate:limit:exceeded:" + LocalDateTime.now().toLocalDate();
        String field = clientId + ":" + endpoint;
        redisTemplate.opsForHash().increment(key, field, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
        
        log.warn("Rate limit exceeded for client: {} on endpoint: {}", clientId, endpoint);
    }

    /**
     * Record cache hit
     */
    public void recordCacheHit(String cacheName) {
        cacheHitCounter.increment();
        
        String key = "cache:hits:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, cacheName, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * Record cache miss
     */
    public void recordCacheMiss(String cacheName) {
        cacheMissCounter.increment();
        
        String key = "cache:misses:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, cacheName, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * Record audit log entry
     */
    public void recordAuditLog(String action, long processingTimeMs) {
        auditLogCounter.increment();
        auditLogProcessingTimer.record(processingTimeMs, TimeUnit.MILLISECONDS);
        
        String key = "audit:logs:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, action, 1);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }

    /**
     * Record database query
     */
    public void recordDatabaseQuery(String queryType, long durationMs) {
        databaseQueryTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        String key = "database:queries:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, queryType, 1);
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    /**
     * Record compliance report generation
     */
    public void recordComplianceReport(String reportType) {
        complianceReportCounter.increment();
        
        String key = "compliance:reports:" + LocalDateTime.now().toLocalDate();
        redisTemplate.opsForHash().increment(key, reportType, 1);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        
        log.info("Compliance report generated: {}", reportType);
    }

    /**
     * Record risk assessment
     */
    public void recordRiskAssessment(String username, long processingTimeMs, double riskScore) {
        riskAssessmentTimer.record(processingTimeMs, TimeUnit.MILLISECONDS);
        
        String key = "risk:assessment:" + LocalDateTime.now().toLocalDate();
        Map<String, Object> assessmentData = new HashMap<>();
        assessmentData.put("username", username);
        assessmentData.put("riskScore", riskScore);
        assessmentData.put("processingTime", processingTimeMs);
        assessmentData.put("timestamp", LocalDateTime.now());
        
        redisTemplate.opsForList().leftPush(key, assessmentData);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }

    /**
     * Get real-time system metrics
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get today's metrics from Redis
        String today = LocalDateTime.now().toLocalDate().toString();
        
        metrics.put("loginSuccessToday", getHashSum("login:success:" + today));
        metrics.put("loginFailureToday", getHashSum("login:failure:" + today));
        metrics.put("securityAlertsToday", getListSize("security:alerts:" + today));
        metrics.put("anomaliesDetectedToday", getListSize("anomaly:detection:" + today));
        metrics.put("apiRequestsToday", getHashSum("api:requests:" + today));
        metrics.put("rateLimitViolationsToday", getHashSum("rate:limit:exceeded:" + today));
        metrics.put("auditLogsToday", getHashSum("audit:logs:" + today));
        
        return metrics;
    }

    /**
     * Scheduled task to clean up old metrics
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM daily
    public void cleanupOldMetrics() {
        log.info("Starting cleanup of old metrics data");
        
        // This would typically clean up metrics older than retention period
        // Implementation depends on specific requirements
        
        log.info("Metrics cleanup completed");
    }

    // Helper methods
    private long getHashSum(String key) {
        try {
            return redisTemplate.opsForHash().values(key).stream()
                    .mapToLong(value -> Long.parseLong(value.toString()))
                    .sum();
        } catch (Exception e) {
            return 0L;
        }
    }

    private long getListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            return 0L;
        }
    }
}
