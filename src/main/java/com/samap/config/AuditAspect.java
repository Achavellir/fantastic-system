package com.samap.config;

import com.samap.model.AuditLog;
import com.samap.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Aspect for automatic audit logging using AOP
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Custom annotation for audit logging
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Auditable {
        String action() default "";
        String resource() default "";
        boolean logParameters() default false;
        boolean logResult() default false;
        boolean highRisk() default false;
        String[] sensitiveFields() default {};
        boolean skipAudit() default false;
    }

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {}

    /**
     * Pointcut for methods annotated with @Auditable
     */
    @Pointcut("@annotation(auditable)")
    public void auditableMethods(Auditable auditable) {}

    /**
     * Around advice for auditable methods
     */
    @Around("auditableMethods(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        String username = getCurrentUsername();
        HttpServletRequest request = getCurrentRequest();
        
        String action = auditable.action().isEmpty() ? 
            joinPoint.getSignature().getName().toUpperCase() : auditable.action();
        String resource = auditable.resource().isEmpty() ? 
            joinPoint.getTarget().getClass().getSimpleName() : auditable.resource();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // Log successful execution
            String details = buildAuditDetails(joinPoint, auditable, result, true);
            auditService.logApiAccess(
                username,
                getEndpoint(request),
                request.getMethod(),
                auditable.logParameters() ? getRequestParameters(joinPoint) : null,
                200, // Assume success
                executionTime,
                getClientIpAddress(request),
                request.getHeader("User-Agent"),
                request.getSession(false) != null ? request.getSession().getId() : null
            );

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // Log failed execution
            String details = buildAuditDetails(joinPoint, auditable, null, false) + 
                "; Error: " + e.getMessage();
            
            auditService.logSecurityEvent(
                username,
                action + "_FAILED",
                details,
                getClientIpAddress(request),
                request.getHeader("User-Agent"),
                AuditLog.ActionStatus.FAILURE
            );

            throw e;
        }
    }

    /**
     * After advice for successful controller method execution
     */
    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logControllerSuccess(JoinPoint joinPoint, Object result) {
        try {
            String username = getCurrentUsername();
            HttpServletRequest request = getCurrentRequest();
            
            if (request != null && !isPublicEndpoint(request.getServletPath())) {
                auditService.logApiAccess(
                    username,
                    getEndpoint(request),
                    request.getMethod(),
                    null, // Don't log parameters by default
                    200,
                    null, // Execution time not available here
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    request.getSession(false) != null ? request.getSession().getId() : null
                );
            }
        } catch (Exception e) {
            log.warn("Failed to log controller success: {}", e.getMessage());
        }
    }

    /**
     * After throwing advice for failed controller method execution
     */
    @AfterThrowing(pointcut = "controllerMethods()", throwing = "exception")
    public void logControllerFailure(JoinPoint joinPoint, Exception exception) {
        try {
            String username = getCurrentUsername();
            HttpServletRequest request = getCurrentRequest();
            
            if (request != null && !isPublicEndpoint(request.getServletPath())) {
                auditService.logSecurityEvent(
                    username,
                    "API_ERROR",
                    "Controller method failed: " + joinPoint.getSignature().getName() + 
                    "; Error: " + exception.getMessage(),
                    getClientIpAddress(request),
                    request.getHeader("User-Agent"),
                    AuditLog.ActionStatus.FAILURE
                );
            }
        } catch (Exception e) {
            log.warn("Failed to log controller failure: {}", e.getMessage());
        }
    }

    // Helper methods
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return "unknown";
        
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

    private String getEndpoint(HttpServletRequest request) {
        return request != null ? request.getRequestURI() : "unknown";
    }

    private String getRequestParameters(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        StringBuilder params = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) params.append(", ");
            
            Object arg = args[i];
            if (arg instanceof HttpServletRequest) {
                params.append("HttpServletRequest");
            } else if (arg != null) {
                // Don't log sensitive data
                String argString = arg.toString();
                if (argString.toLowerCase().contains("password")) {
                    params.append("[REDACTED]");
                } else {
                    params.append(argString.length() > 100 ? 
                        argString.substring(0, 100) + "..." : argString);
                }
            } else {
                params.append("null");
            }
        }
        
        return params.toString();
    }

    private String buildAuditDetails(JoinPoint joinPoint, Auditable auditable, 
                                   Object result, boolean success) {
        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(joinPoint.getSignature().getName());
        
        if (auditable.logParameters()) {
            String params = getRequestParameters(joinPoint);
            if (params != null) {
                details.append("; Parameters: ").append(params);
            }
        }
        
        if (success && auditable.logResult() && result != null) {
            String resultString = result.toString();
            details.append("; Result: ").append(
                resultString.length() > 200 ? 
                resultString.substring(0, 200) + "..." : resultString
            );
        }
        
        return details.toString();
    }

    private boolean isPublicEndpoint(String path) {
        if (path == null) return false;
        
        String[] publicPaths = {
            "/api/auth/",
            "/api/public/",
            "/actuator/health",
            "/swagger-ui/",
            "/v3/api-docs/"
        };

        for (String publicPath : publicPaths) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }

        return false;
    }
}
