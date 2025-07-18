package com.samap.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Comprehensive health check service for system monitoring
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService implements HealthIndicator {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;

        // Check database health
        try {
            checkDatabaseHealth();
            details.put("database", "UP");
        } catch (Exception e) {
            details.put("database", "DOWN - " + e.getMessage());
            isHealthy = false;
        }

        // Check Redis health
        try {
            checkRedisHealth();
            details.put("redis", "UP");
        } catch (Exception e) {
            details.put("redis", "DOWN - " + e.getMessage());
            isHealthy = false;
        }

        // Check system resources
        try {
            Map<String, Object> systemHealth = checkSystemHealth();
            details.put("system", systemHealth);
        } catch (Exception e) {
            details.put("system", "ERROR - " + e.getMessage());
        }

        // Add application metrics
        details.put("timestamp", LocalDateTime.now());
        details.put("version", "1.0.0");
        details.put("environment", System.getProperty("spring.profiles.active", "default"));

        return isHealthy ? Health.up().withDetails(details).build() 
                        : Health.down().withDetails(details).build();
    }

    /**
     * Check database connectivity and performance
     */
    public void checkDatabaseHealth() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            if (!connection.isValid(5)) {
                throw new Exception("Database connection is not valid");
            }
            
            // Test a simple query
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1");
            if (!resultSet.next()) {
                throw new Exception("Database query test failed");
            }
        }
    }

    /**
     * Check Redis connectivity and performance
     */
    public void checkRedisHealth() throws Exception {
        try {
            // Test Redis connection with ping
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            
            if (!"PONG".equals(pong)) {
                throw new Exception("Redis ping test failed");
            }

            // Test basic operations
            String testKey = "health:check:" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(testKey, "test", 10, java.util.concurrent.TimeUnit.SECONDS);
            String value = (String) redisTemplate.opsForValue().get(testKey);
            
            if (!"test".equals(value)) {
                throw new Exception("Redis read/write test failed");
            }
            
            redisTemplate.delete(testKey);
        } catch (Exception e) {
            throw new Exception("Redis health check failed: " + e.getMessage());
        }
    }

    /**
     * Check system resources and performance
     */
    public Map<String, Object> checkSystemHealth() {
        Map<String, Object> systemHealth = new HashMap<>();
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", formatBytes(totalMemory));
        memory.put("used", formatBytes(usedMemory));
        memory.put("free", formatBytes(freeMemory));
        memory.put("max", formatBytes(maxMemory));
        memory.put("usagePercentage", Math.round((double) usedMemory / totalMemory * 100));
        
        systemHealth.put("memory", memory);
        
        // CPU information
        int availableProcessors = runtime.availableProcessors();
        systemHealth.put("cpu", Map.of(
            "availableProcessors", availableProcessors,
            "systemLoadAverage", getSystemLoadAverage()
        ));
        
        // JVM information
        systemHealth.put("jvm", Map.of(
            "version", System.getProperty("java.version"),
            "vendor", System.getProperty("java.vendor"),
            "uptime", getJvmUptime()
        ));
        
        // Disk space (simplified)
        systemHealth.put("disk", getDiskSpace());
        
        return systemHealth;
    }

    /**
     * Get detailed application health status
     */
    public Map<String, Object> getDetailedHealthStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Database status
        try {
            long startTime = System.currentTimeMillis();
            checkDatabaseHealth();
            long responseTime = System.currentTimeMillis() - startTime;
            
            status.put("database", Map.of(
                "status", "UP",
                "responseTime", responseTime + "ms",
                "lastChecked", LocalDateTime.now()
            ));
        } catch (Exception e) {
            status.put("database", Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "lastChecked", LocalDateTime.now()
            ));
        }
        
        // Redis status
        try {
            long startTime = System.currentTimeMillis();
            checkRedisHealth();
            long responseTime = System.currentTimeMillis() - startTime;
            
            status.put("redis", Map.of(
                "status", "UP",
                "responseTime", responseTime + "ms",
                "lastChecked", LocalDateTime.now()
            ));
        } catch (Exception e) {
            status.put("redis", Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "lastChecked", LocalDateTime.now()
            ));
        }
        
        // System resources
        status.put("system", checkSystemHealth());
        
        // Application info
        status.put("application", Map.of(
            "name", "SAMAP",
            "version", "1.0.0",
            "startTime", getApplicationStartTime(),
            "uptime", getApplicationUptime()
        ));
        
        return status;
    }

    // Helper methods
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private double getSystemLoadAverage() {
        try {
            return ((com.sun.management.OperatingSystemMXBean) 
                java.lang.management.ManagementFactory.getOperatingSystemMXBean())
                .getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return -1.0;
        }
    }

    private String getJvmUptime() {
        long uptime = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = uptime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
    }

    private Map<String, Object> getDiskSpace() {
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            return Map.of(
                "total", formatBytes(totalSpace),
                "used", formatBytes(usedSpace),
                "free", formatBytes(freeSpace),
                "usagePercentage", Math.round((double) usedSpace / totalSpace * 100)
            );
        } catch (Exception e) {
            return Map.of("error", "Unable to retrieve disk space information");
        }
    }

    private String getApplicationStartTime() {
        long startTime = java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startTime), 
            java.time.ZoneId.systemDefault()
        ).toString();
    }

    private String getApplicationUptime() {
        return getJvmUptime();
    }
}
