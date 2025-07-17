package com.samap.repository;

import com.samap.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity with security analytics queries
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by username
     */
    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByActionOrderByTimestampDesc(String action, Pageable pageable);

    /**
     * Find audit logs by status
     */
    Page<AuditLog> findByStatusOrderByTimestampDesc(AuditLog.ActionStatus status, Pageable pageable);

    /**
     * Find audit logs by risk level
     */
    Page<AuditLog> findByRiskLevelOrderByTimestampDesc(AuditLog.RiskLevel riskLevel, Pageable pageable);

    /**
     * Find audit logs within date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);

    /**
     * Find failed login attempts
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' ORDER BY a.timestamp DESC")
    Page<AuditLog> findFailedLoginAttempts(Pageable pageable);

    /**
     * Find failed login attempts by username
     */
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username AND a.action = 'LOGIN_FAILED' ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttemptsByUsername(@Param("username") String username);

    /**
     * Find failed login attempts by IP address
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.action = 'LOGIN_FAILED' ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttemptsByIp(@Param("ipAddress") String ipAddress);

    /**
     * Find high-risk activities
     */
    @Query("SELECT a FROM AuditLog a WHERE a.riskLevel IN ('HIGH', 'CRITICAL') ORDER BY a.timestamp DESC")
    Page<AuditLog> findHighRiskActivities(Pageable pageable);

    /**
     * Find anomalous activities
     */
    Page<AuditLog> findByIsAnomalyTrueOrderByTimestampDesc(Pageable pageable);

    /**
     * Find activities by IP address
     */
    Page<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress, Pageable pageable);

    /**
     * Search audit logs
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.action) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.resource) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.ipAddress) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogs(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count failed login attempts in time period
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' AND a.timestamp >= :since")
    long countFailedLoginsSince(@Param("since") LocalDateTime since);

    /**
     * Count failed login attempts by username in time period
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username AND a.action = 'LOGIN_FAILED' AND a.timestamp >= :since")
    long countFailedLoginsByUserSince(@Param("username") String username, @Param("since") LocalDateTime since);

    /**
     * Count failed login attempts by IP in time period
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.action = 'LOGIN_FAILED' AND a.timestamp >= :since")
    long countFailedLoginsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Get activity statistics
     */
    @Query("SELECT " +
           "COUNT(a) as totalActivities, " +
           "SUM(CASE WHEN a.status = 'SUCCESS' THEN 1 ELSE 0 END) as successfulActivities, " +
           "SUM(CASE WHEN a.status = 'FAILURE' THEN 1 ELSE 0 END) as failedActivities, " +
           "SUM(CASE WHEN a.riskLevel IN ('HIGH', 'CRITICAL') THEN 1 ELSE 0 END) as highRiskActivities, " +
           "SUM(CASE WHEN a.isAnomaly = true THEN 1 ELSE 0 END) as anomalousActivities " +
           "FROM AuditLog a WHERE a.timestamp >= :since")
    Object[] getActivityStatistics(@Param("since") LocalDateTime since);

    /**
     * Get top active users
     */
    @Query("SELECT a.username, COUNT(a) as activityCount FROM AuditLog a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY a.username " +
           "ORDER BY activityCount DESC")
    List<Object[]> getTopActiveUsers(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Get top IP addresses by activity
     */
    @Query("SELECT a.ipAddress, COUNT(a) as activityCount FROM AuditLog a " +
           "WHERE a.timestamp >= :since AND a.ipAddress IS NOT NULL " +
           "GROUP BY a.ipAddress " +
           "ORDER BY activityCount DESC")
    List<Object[]> getTopIpAddresses(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Get activity trends by hour
     */
    @Query("SELECT HOUR(a.timestamp) as hour, COUNT(a) as activityCount FROM AuditLog a " +
           "WHERE a.timestamp >= :since " +
           "GROUP BY HOUR(a.timestamp) " +
           "ORDER BY hour")
    List<Object[]> getActivityTrendsByHour(@Param("since") LocalDateTime since);

    /**
     * Delete old audit logs
     */
    void deleteByTimestampBefore(LocalDateTime cutoffDate);


}
