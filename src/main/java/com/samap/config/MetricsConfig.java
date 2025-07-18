package com.samap.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom metrics configuration for monitoring
 */
@Configuration
@RequiredArgsConstructor
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    // Atomic counters for real-time metrics
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger failedLogins = new AtomicInteger(0);
    private final AtomicInteger highRiskActivities = new AtomicInteger(0);

    /**
     * Authentication metrics
     */
    @Bean
    public Counter loginSuccessCounter() {
        return Counter.builder("samap_login_success_total")
                .description("Total number of successful logins")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginFailureCounter() {
        return Counter.builder("samap_login_failure_total")
                .description("Total number of failed login attempts")
                .register(meterRegistry);
    }

    @Bean
    public Timer loginDurationTimer() {
        return Timer.builder("samap_login_duration")
                .description("Login request duration")
                .register(meterRegistry);
    }

    /**
     * Security metrics
     */
    @Bean
    public Counter securityAlertsCounter() {
        return Counter.builder("samap_security_alerts_total")
                .description("Total number of security alerts")
                .register(meterRegistry);
    }

    @Bean
    public Counter anomalyDetectionCounter() {
        return Counter.builder("samap_anomaly_detection_total")
                .description("Total number of anomalies detected")
                .register(meterRegistry);
    }

    @Bean
    public Gauge highRiskActivitiesGauge() {
        return Gauge.builder("samap_high_risk_activities", highRiskActivities, AtomicInteger::doubleValue)
                .description("Current number of high risk activities")
                .register(meterRegistry);
    }

    /**
     * User metrics
     */
    @Bean
    public Gauge activeUsersGauge() {
        return Gauge.builder("samap_active_users", activeUsers, AtomicInteger::doubleValue)
                .description("Current number of active users")
                .register(meterRegistry);
    }

    @Bean
    public Counter userCreationCounter() {
        return Counter.builder("samap_user_creation_total")
                .description("Total number of users created")
                .register(meterRegistry);
    }

    @Bean
    public Counter accountLockoutCounter() {
        return Counter.builder("samap_account_lockout_total")
                .description("Total number of account lockouts")
                .register(meterRegistry);
    }

    /**
     * API metrics
     */
    @Bean
    public Timer apiRequestTimer() {
        return Timer.builder("samap_api_request_duration")
                .description("API request duration")
                .register(meterRegistry);
    }

    @Bean
    public Counter rateLimitExceededCounter() {
        return Counter.builder("samap_rate_limit_exceeded_total")
                .description("Total number of rate limit violations")
                .register(meterRegistry);
    }

    /**
     * Database metrics
     */
    @Bean
    public Timer databaseQueryTimer() {
        return Timer.builder("samap_database_query_duration")
                .description("Database query duration")
                .register(meterRegistry);
    }

    /**
     * Cache metrics
     */
    @Bean
    public Counter cacheHitCounter() {
        return Counter.builder("samap_cache_hit_total")
                .description("Total number of cache hits")
                .register(meterRegistry);
    }

    @Bean
    public Counter cacheMissCounter() {
        return Counter.builder("samap_cache_miss_total")
                .description("Total number of cache misses")
                .register(meterRegistry);
    }

    /**
     * Audit metrics
     */
    @Bean
    public Counter auditLogCounter() {
        return Counter.builder("samap_audit_log_total")
                .description("Total number of audit log entries")
                .register(meterRegistry);
    }

    @Bean
    public Timer auditLogProcessingTimer() {
        return Timer.builder("samap_audit_log_processing_duration")
                .description("Audit log processing duration")
                .register(meterRegistry);
    }

    /**
     * System health metrics
     */
    @Bean
    public Gauge jvmMemoryUsageGauge() {
        return Gauge.builder("samap_jvm_memory_usage", this, MetricsConfig::getJvmMemoryUsage)
                .description("JVM memory usage percentage")
                .register(meterRegistry);
    }

    @Bean
    public Gauge redisConnectionsGauge() {
        return Gauge.builder("samap_redis_connections", this, MetricsConfig::getRedisConnections)
                .description("Number of Redis connections")
                .register(meterRegistry);
    }

    /**
     * Business metrics
     */
    @Bean
    public Counter complianceReportCounter() {
        return Counter.builder("samap_compliance_report_total")
                .description("Total number of compliance reports generated")
                .register(meterRegistry);
    }

    @Bean
    public Timer riskAssessmentTimer() {
        return Timer.builder("samap_risk_assessment_duration")
                .description("Risk assessment processing duration")
                .register(meterRegistry);
    }

    // Helper methods for gauge calculations
    private double getJvmMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return ((double) (totalMemory - freeMemory) / totalMemory) * 100;
    }

    private double getRedisConnections() {
        try {
            // This is a simplified example - in production you'd get actual connection count
            return 1.0; // Placeholder
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Methods to update atomic counters
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    public void incrementFailedLogins() {
        failedLogins.incrementAndGet();
    }

    public void incrementHighRiskActivities() {
        highRiskActivities.incrementAndGet();
    }

    public void decrementHighRiskActivities() {
        highRiskActivities.decrementAndGet();
    }
}
