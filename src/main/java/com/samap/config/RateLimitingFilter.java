package com.samap.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Rate limiting filter to prevent abuse
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientId = getClientIdentifier(request);
        String requestPath = request.getRequestURI();
        
        // Skip rate limiting for health checks and static resources
        if (shouldSkipRateLimit(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean allowed = checkRateLimit(clientId, requestPath);
        
        if (!allowed) {
            handleRateLimitExceeded(response, clientId, requestPath);
            return;
        }

        // Add rate limit headers
        addRateLimitHeaders(response, clientId, requestPath);
        
        filterChain.doFilter(request, response);
    }

    /**
     * Get client identifier for rate limiting
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get user from JWT token first
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract username from JWT if available
            // For now, use IP address as fallback
        }
        
        // Use IP address as client identifier
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Check if request should skip rate limiting
     */
    private boolean shouldSkipRateLimit(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/static/") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.equals("/favicon.ico");
    }

    /**
     * Check rate limit based on request path
     */
    private boolean checkRateLimit(String clientId, String path) {
        if (path.startsWith("/api/auth/login")) {
            return rateLimitingConfig.isLoginAllowed(clientId);
        } else if (path.startsWith("/api/admin/")) {
            return rateLimitingConfig.isAdminAllowed(clientId);
        } else if (path.startsWith("/api/")) {
            return rateLimitingConfig.isApiAllowed(clientId);
        }
        
        // Default to API rate limit
        return rateLimitingConfig.isApiAllowed(clientId);
    }

    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, String clientId, String path) {
        String type = getRateLimitType(path);
        RateLimitingConfig.RateLimitInfo info = rateLimitingConfig.getRateLimitInfo(clientId, type);
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(info.getRefillRate()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(info.getRemainingTokens()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000)); // 1 minute
    }

    /**
     * Get rate limit type based on path
     */
    private String getRateLimitType(String path) {
        if (path.startsWith("/api/auth/login")) {
            return "login";
        } else if (path.startsWith("/api/admin/")) {
            return "admin";
        } else {
            return "api";
        }
    }

    /**
     * Handle rate limit exceeded
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String clientId, String path) 
            throws IOException {
        
        log.warn("Rate limit exceeded for client: {} on path: {}", clientId, path);
        
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("error", Map.of(
            "code", "RATE_LIMIT_EXCEEDED",
            "message", "Too many requests. Please try again later.",
            "details", "Rate limit exceeded for this client"
        ));
        
        String type = getRateLimitType(path);
        RateLimitingConfig.RateLimitInfo info = rateLimitingConfig.getRateLimitInfo(clientId, type);
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(info.getRefillRate()));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() + 60000));
        response.setHeader("Retry-After", "60");
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
