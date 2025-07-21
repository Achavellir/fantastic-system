package com.samap.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Fallback configuration for when optional dependencies are not available
 */
@Configuration
@Slf4j
public class FallbackConfig {

    /**
     * Fallback meter registry when Prometheus is not available
     */
    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry fallbackMeterRegistry() {
        log.warn("Using fallback SimpleMeterRegistry - metrics will not be exported");
        return new SimpleMeterRegistry();
    }

    /**
     * Fallback Redis template when Redis is not available
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> fallbackRedisTemplate() {
        log.warn("Redis not available - using fallback null template");
        return null; // This will be handled gracefully in services
    }

    /**
     * Fallback metrics beans when MetricsConfig is not available
     */
    @Bean
    @ConditionalOnMissingBean(name = "loginSuccessCounter")
    public Counter loginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_login_success_total")
                .description("Total number of successful logins")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "loginFailureCounter")
    public Counter loginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_login_failure_total")
                .description("Total number of failed login attempts")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "securityAlertsCounter")
    public Counter securityAlertsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_security_alerts_total")
                .description("Total number of security alerts")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "anomalyDetectionCounter")
    public Counter anomalyDetectionCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_anomaly_detection_total")
                .description("Total number of anomalies detected")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "userCreationCounter")
    public Counter userCreationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_user_creation_total")
                .description("Total number of users created")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "accountLockoutCounter")
    public Counter accountLockoutCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_account_lockout_total")
                .description("Total number of account lockouts")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "rateLimitExceededCounter")
    public Counter rateLimitExceededCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_rate_limit_exceeded_total")
                .description("Total number of rate limit violations")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cacheHitCounter")
    public Counter cacheHitCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_cache_hit_total")
                .description("Total number of cache hits")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "cacheMissCounter")
    public Counter cacheMissCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_cache_miss_total")
                .description("Total number of cache misses")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditLogCounter")
    public Counter auditLogCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_audit_log_total")
                .description("Total number of audit log entries")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "complianceReportCounter")
    public Counter complianceReportCounter(MeterRegistry meterRegistry) {
        return Counter.builder("samap_compliance_report_total")
                .description("Total number of compliance reports generated")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "loginDurationTimer")
    public Timer loginDurationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("samap_login_duration")
                .description("Login request duration")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "apiRequestTimer")
    public Timer apiRequestTimer(MeterRegistry meterRegistry) {
        return Timer.builder("samap_api_request_duration")
                .description("API request duration")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "databaseQueryTimer")
    public Timer databaseQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("samap_database_query_duration")
                .description("Database query duration")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "auditLogProcessingTimer")
    public Timer auditLogProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("samap_audit_log_processing_duration")
                .description("Audit log processing duration")
                .register(meterRegistry);
    }

    @Bean
    @ConditionalOnMissingBean(name = "riskAssessmentTimer")
    public Timer riskAssessmentTimer(MeterRegistry meterRegistry) {
        return Timer.builder("samap_risk_assessment_duration")
                .description("Risk assessment processing duration")
                .register(meterRegistry);
    }
}
