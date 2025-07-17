package com.samap.controller;

import com.samap.config.AuditAspect.Auditable;
import com.samap.model.AuditLog;
import com.samap.service.AuditService;
import com.samap.service.AuditService.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit controller for security monitoring and log analysis
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    /**
     * Get all audit logs with pagination
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_VIEW", resource = "AUDIT")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getAuditLogs(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs by username
     */
    @GetMapping("/logs/user/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or #username == authentication.name")
    @Auditable(action = "AUDIT_VIEW_USER", resource = "AUDIT", logParameters = true)
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUsername(
            @PathVariable String username,
            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditService.getAuditLogsByUsername(username, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs by action
     */
    @GetMapping("/logs/action/{action}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_VIEW_ACTION", resource = "AUDIT", logParameters = true)
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @PathVariable String action,
            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditService.getAuditLogsByAction(action, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get high-risk activities
     */
    @GetMapping("/logs/high-risk")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('SECURITY_OFFICER')")
    @Auditable(action = "AUDIT_VIEW_HIGH_RISK", resource = "AUDIT")
    public ResponseEntity<Page<AuditLog>> getHighRiskActivities(Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getHighRiskActivities(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get anomalous activities
     */
    @GetMapping("/logs/anomalies")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('SECURITY_OFFICER')")
    @Auditable(action = "AUDIT_VIEW_ANOMALIES", resource = "AUDIT")
    public ResponseEntity<Page<AuditLog>> getAnomalousActivities(Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getAnomalousActivities(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get failed login attempts
     */
    @GetMapping("/logs/failed-logins")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR') or hasRole('SECURITY_OFFICER')")
    @Auditable(action = "AUDIT_VIEW_FAILED_LOGINS", resource = "AUDIT")
    public ResponseEntity<Page<AuditLog>> getFailedLoginAttempts(Pageable pageable) {
        Page<AuditLog> auditLogs = auditService.getFailedLoginAttempts(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Search audit logs
     */
    @GetMapping("/logs/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_SEARCH", resource = "AUDIT", logParameters = true)
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @RequestParam String query,
            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditService.searchAuditLogs(query, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs within date range
     */
    @GetMapping("/logs/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_VIEW_DATE_RANGE", resource = "AUDIT", logParameters = true)
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditService.getAuditLogsByDateRange(startDate, endDate, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get activity statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_STATISTICS", resource = "AUDIT")
    public ResponseEntity<ActivityStatistics> getActivityStatistics(
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        ActivityStatistics statistics = auditService.getActivityStatistics(since);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get top active users
     */
    @GetMapping("/statistics/top-users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_TOP_USERS", resource = "AUDIT")
    public ResponseEntity<List<UserActivity>> getTopActiveUsers(
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "10") int limit,
            Pageable pageable) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<UserActivity> topUsers = auditService.getTopActiveUsers(since, pageable);
        return ResponseEntity.ok(topUsers);
    }

    /**
     * Get activity trends by hour
     */
    @GetMapping("/statistics/hourly-trends")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "AUDIT_HOURLY_TRENDS", resource = "AUDIT")
    public ResponseEntity<List<HourlyActivity>> getActivityTrendsByHour(
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<HourlyActivity> trends = auditService.getActivityTrendsByHour(since);
        return ResponseEntity.ok(trends);
    }

    /**
     * Export audit logs (placeholder for future implementation)
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') or hasPermission('AUDIT_EXPORT')")
    @Auditable(action = "AUDIT_EXPORT", resource = "AUDIT", logParameters = true)
    public ResponseEntity<String> exportAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "CSV") String format) {
        
        // TODO: Implement audit log export functionality
        // This would typically generate CSV, Excel, or PDF reports
        
        return ResponseEntity.ok("Export functionality will be implemented in future version");
    }
}
