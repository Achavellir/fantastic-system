package com.samap.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Audit Log entity for comprehensive security auditing
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "username"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_ip", columnList = "ipAddress"),
    @Index(name = "idx_audit_status", columnList = "status"),
    @Index(name = "idx_audit_risk_score", columnList = "riskScore")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 100)
    private String resource;

    @Column(name = "resource_id")
    private String resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionStatus status;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(length = 100)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(name = "risk_score")
    private Double riskScore = 0.0;

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors;

    @Column(name = "location_country", length = 50)
    private String locationCountry;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly = false;

    @Column(name = "anomaly_reasons", columnDefinition = "TEXT")
    private String anomalyReasons;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    public enum ActionStatus {
        SUCCESS,
        FAILURE,
        BLOCKED,
        WARNING
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // Predefined action types
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTION_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String ACTION_USER_CREATE = "USER_CREATE";
    public static final String ACTION_USER_UPDATE = "USER_UPDATE";
    public static final String ACTION_USER_DELETE = "USER_DELETE";
    public static final String ACTION_ROLE_ASSIGN = "ROLE_ASSIGN";
    public static final String ACTION_ROLE_REVOKE = "ROLE_REVOKE";
    public static final String ACTION_PERMISSION_GRANT = "PERMISSION_GRANT";
    public static final String ACTION_PERMISSION_REVOKE = "PERMISSION_REVOKE";
    public static final String ACTION_DATA_ACCESS = "DATA_ACCESS";
    public static final String ACTION_DATA_EXPORT = "DATA_EXPORT";
    public static final String ACTION_SYSTEM_CONFIG = "SYSTEM_CONFIG";
    public static final String ACTION_SECURITY_ALERT = "SECURITY_ALERT";

    // Convenience methods
    public boolean isHighRisk() {
        return RiskLevel.HIGH.equals(riskLevel) || RiskLevel.CRITICAL.equals(riskLevel);
    }

    public boolean isFailedAction() {
        return ActionStatus.FAILURE.equals(status) || ActionStatus.BLOCKED.equals(status);
    }

    public void setRiskLevelFromScore() {
        if (riskScore == null) {
            this.riskLevel = RiskLevel.LOW;
        } else if (riskScore >= 0.8) {
            this.riskLevel = RiskLevel.CRITICAL;
        } else if (riskScore >= 0.6) {
            this.riskLevel = RiskLevel.HIGH;
        } else if (riskScore >= 0.3) {
            this.riskLevel = RiskLevel.MEDIUM;
        } else {
            this.riskLevel = RiskLevel.LOW;
        }
    }
}
