package com.samap.controller;

import com.samap.config.AuditAspect.Auditable;
import com.samap.service.AuthenticationService;
import com.samap.service.AuthenticationService.AuthenticationRequest;
import com.samap.service.AuthenticationService.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication controller for login, logout, and token management
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Auditable(action = "LOGIN", resource = "AUTH", logParameters = false)
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login attempt for user: {}", request.getUsername());
        AuthenticationResponse response = authenticationService.authenticate(request, httpRequest);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh JWT token endpoint
     */
    @PostMapping("/refresh")
    @Auditable(action = "TOKEN_REFRESH", resource = "AUTH")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        AuthenticationResponse response = authenticationService.refreshToken(refreshToken, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * User logout endpoint
     */
    @PostMapping("/logout")
    @Auditable(action = "LOGOUT", resource = "AUTH")
    public ResponseEntity<Map<String, String>> logout(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        if (authentication != null) {
            authenticationService.logout(authentication.getName(), httpRequest);
        }
        
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Validate token endpoint
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            Authentication authentication) {
        
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
            ));
        }
        
        return ResponseEntity.ok(Map.of("valid", false));
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities(),
                "authenticated", true
            ));
        }
        
        return ResponseEntity.ok(Map.of("authenticated", false));
    }
}
