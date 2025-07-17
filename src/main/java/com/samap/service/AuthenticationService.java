package com.samap.service;

import com.samap.model.AuditLog;
import com.samap.model.User;
import com.samap.repository.UserRepository;
import com.samap.service.CustomUserDetailsService.CustomUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication service handling login, logout, and security events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final PasswordEncoder passwordEncoder;
    private final RiskAssessmentService riskAssessmentService;
    private final SecurityMonitoringService securityMonitoringService;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Authenticate user and generate JWT tokens
     */
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        try {
            // Check if user exists and is not locked
            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Check account status
            if (!user.isAccountActive()) {
                auditService.logSecurityEvent(username, AuditLog.ACTION_LOGIN_FAILED, 
                    "Account is not active", ipAddress, userAgent, AuditLog.ActionStatus.BLOCKED);
                throw new DisabledException("Account is disabled");
            }

            // Check if account is locked
            if (user.isAccountLocked()) {
                auditService.logSecurityEvent(username, AuditLog.ACTION_LOGIN_FAILED, 
                    "Account is locked", ipAddress, userAgent, AuditLog.ActionStatus.BLOCKED);
                throw new LockedException("Account is locked until " + user.getAccountLockedUntil());
            }

            // Perform authentication
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            
            // Reset failed attempts on successful login
            user.resetFailedLoginAttempts();
            user.setLastLogin(LocalDateTime.now());
            user.setLastLoginIp(ipAddress);
            userRepository.save(user);

            // Generate tokens (using compact format for smaller size)
            String accessToken = jwtService.generateCompactToken(userPrincipal);
            String refreshToken = jwtService.generateRefreshToken(userPrincipal);

            // Calculate risk score
            double riskScore = riskAssessmentService.calculateLoginRiskScore(user, ipAddress, userAgent);

            // Log successful login
            auditService.logSecurityEvent(username, AuditLog.ACTION_LOGIN,
                "Successful login", ipAddress, userAgent, AuditLog.ActionStatus.SUCCESS, riskScore);

            // Monitor for high-risk login
            if (riskScore > 0.5) {
                AuditLog auditLog = new AuditLog();
                auditLog.setUsername(username);
                auditLog.setAction(AuditLog.ACTION_LOGIN);
                auditLog.setIpAddress(ipAddress);
                auditLog.setRiskScore(riskScore);
                auditLog.setTimestamp(LocalDateTime.now());
                securityMonitoringService.monitorHighRiskActivity(auditLog);
            }

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(createUserResponse(user))
                    .riskScore(riskScore)
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedLogin(username, ipAddress, userAgent, "Invalid credentials");
            throw e;
        } catch (AuthenticationException e) {
            handleFailedLogin(username, ipAddress, userAgent, e.getMessage());
            throw e;
        }
    }

    /**
     * Handle failed login attempts
     */
    @Transactional
    private void handleFailedLogin(String username, String ipAddress, String userAgent, String reason) {
        userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.lockAccount(LOCKOUT_DURATION_MINUTES);
                auditService.logSecurityEvent(username, AuditLog.ACTION_ACCOUNT_LOCKED, 
                    "Account locked due to failed login attempts", ipAddress, userAgent, 
                    AuditLog.ActionStatus.BLOCKED);
                log.warn("Account locked for user: {} due to {} failed attempts", username, 
                    user.getFailedLoginAttempts());
            }
            
            userRepository.save(user);
        });

        // Log failed login attempt
        auditService.logSecurityEvent(username, AuditLog.ACTION_LOGIN_FAILED,
            reason, ipAddress, userAgent, AuditLog.ActionStatus.FAILURE);

        // Monitor failed login for security threats
        securityMonitoringService.monitorFailedLogin(username, ipAddress);
    }

    /**
     * Refresh JWT token
     */
    public AuthenticationResponse refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        try {
            String username = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByUsernameIgnoreCase(username)
                    .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

            CustomUserPrincipal userPrincipal = new CustomUserPrincipal(user);
            
            if (jwtService.isTokenValid(refreshToken, userPrincipal)) {
                String newAccessToken = jwtService.generateCompactToken(userPrincipal);
                
                // Log token refresh
                auditService.logSecurityEvent(username, "TOKEN_REFRESH", 
                    "Access token refreshed", getClientIpAddress(httpRequest), 
                    httpRequest.getHeader("User-Agent"), AuditLog.ActionStatus.SUCCESS);

                return AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtService.getExpirationTime())
                        .user(createUserResponse(user))
                        .build();
            } else {
                throw new BadCredentialsException("Invalid refresh token");
            }
        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    /**
     * Logout user
     */
    public void logout(String username, HttpServletRequest httpRequest) {
        auditService.logSecurityEvent(username, AuditLog.ACTION_LOGOUT, 
            "User logged out", getClientIpAddress(httpRequest), 
            httpRequest.getHeader("User-Agent"), AuditLog.ActionStatus.SUCCESS);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
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
     * Create user response DTO
     */
    private UserResponse createUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(role -> role.getName()).toList())
                .lastLogin(user.getLastLogin())
                .build();
    }

    // DTOs
    public static class AuthenticationRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthenticationResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private long expiresIn;
        private UserResponse user;
        private double riskScore;

        public static AuthenticationResponseBuilder builder() {
            return new AuthenticationResponseBuilder();
        }

        // Getters and setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
        public UserResponse getUser() { return user; }
        public void setUser(UserResponse user) { this.user = user; }
        public double getRiskScore() { return riskScore; }
        public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

        public static class AuthenticationResponseBuilder {
            private String accessToken;
            private String refreshToken;
            private String tokenType;
            private long expiresIn;
            private UserResponse user;
            private double riskScore;

            public AuthenticationResponseBuilder accessToken(String accessToken) {
                this.accessToken = accessToken;
                return this;
            }

            public AuthenticationResponseBuilder refreshToken(String refreshToken) {
                this.refreshToken = refreshToken;
                return this;
            }

            public AuthenticationResponseBuilder tokenType(String tokenType) {
                this.tokenType = tokenType;
                return this;
            }

            public AuthenticationResponseBuilder expiresIn(long expiresIn) {
                this.expiresIn = expiresIn;
                return this;
            }

            public AuthenticationResponseBuilder user(UserResponse user) {
                this.user = user;
                return this;
            }

            public AuthenticationResponseBuilder riskScore(double riskScore) {
                this.riskScore = riskScore;
                return this;
            }

            public AuthenticationResponse build() {
                AuthenticationResponse response = new AuthenticationResponse();
                response.accessToken = this.accessToken;
                response.refreshToken = this.refreshToken;
                response.tokenType = this.tokenType;
                response.expiresIn = this.expiresIn;
                response.user = this.user;
                response.riskScore = this.riskScore;
                return response;
            }
        }
    }

    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private java.util.List<String> roles;
        private LocalDateTime lastLogin;

        public static UserResponseBuilder builder() {
            return new UserResponseBuilder();
        }

        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public java.util.List<String> getRoles() { return roles; }
        public void setRoles(java.util.List<String> roles) { this.roles = roles; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

        public static class UserResponseBuilder {
            private Long id;
            private String username;
            private String email;
            private String firstName;
            private String lastName;
            private java.util.List<String> roles;
            private LocalDateTime lastLogin;

            public UserResponseBuilder id(Long id) {
                this.id = id;
                return this;
            }

            public UserResponseBuilder username(String username) {
                this.username = username;
                return this;
            }

            public UserResponseBuilder email(String email) {
                this.email = email;
                return this;
            }

            public UserResponseBuilder firstName(String firstName) {
                this.firstName = firstName;
                return this;
            }

            public UserResponseBuilder lastName(String lastName) {
                this.lastName = lastName;
                return this;
            }

            public UserResponseBuilder roles(java.util.List<String> roles) {
                this.roles = roles;
                return this;
            }

            public UserResponseBuilder lastLogin(LocalDateTime lastLogin) {
                this.lastLogin = lastLogin;
                return this;
            }

            public UserResponse build() {
                UserResponse response = new UserResponse();
                response.id = this.id;
                response.username = this.username;
                response.email = this.email;
                response.firstName = this.firstName;
                response.lastName = this.lastName;
                response.roles = this.roles;
                response.lastLogin = this.lastLogin;
                return response;
            }
        }
    }
}
