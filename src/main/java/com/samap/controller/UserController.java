package com.samap.controller;

import com.samap.config.AuditAspect.Auditable;
import com.samap.model.User;
import com.samap.service.UserService;
import com.samap.service.UserService.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User management controller with comprehensive CRUD operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * Get all users with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    @Auditable(action = "USER_LIST", resource = "USER")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Search users
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    @Auditable(action = "USER_SEARCH", resource = "USER", logParameters = true)
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam String query,
            Pageable pageable) {
        
        Page<User> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or #id == authentication.principal.id")
    @Auditable(action = "USER_VIEW", resource = "USER")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new user
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER')")
    @Auditable(action = "USER_CREATE", resource = "USER", logParameters = true)
    public ResponseEntity<User> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            User user = userService.createUser(request, httpRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER_MANAGER') or #id == authentication.principal.id")
    @Auditable(action = "USER_UPDATE", resource = "USER", logParameters = true)
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            User user = userService.updateUser(id, request, httpRequest);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete user
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "USER_DELETE", resource = "USER")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        try {
            userService.deleteUser(id, httpRequest);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assign role to user
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "ROLE_ASSIGN", resource = "USER", logParameters = true)
    public ResponseEntity<Map<String, String>> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            HttpServletRequest httpRequest) {
        
        try {
            userService.assignRole(userId, roleId, httpRequest);
            return ResponseEntity.ok(Map.of("message", "Role assigned successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Revoke role from user
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "ROLE_REVOKE", resource = "USER", logParameters = true)
    public ResponseEntity<Map<String, String>> revokeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            HttpServletRequest httpRequest) {
        
        try {
            userService.revokeRole(userId, roleId, httpRequest);
            return ResponseEntity.ok(Map.of("message", "Role revoked successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Change user password
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Auditable(action = "PASSWORD_CHANGE", resource = "USER", logParameters = false)
    public ResponseEntity<Map<String, String>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            userService.changePassword(id, request, httpRequest);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lock user account
     */
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "ACCOUNT_LOCK", resource = "USER", logParameters = true)
    public ResponseEntity<Map<String, String>> lockUser(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int minutes,
            HttpServletRequest httpRequest) {
        
        try {
            userService.lockUser(id, minutes, httpRequest);
            return ResponseEntity.ok(Map.of("message", "User account locked successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Unlock user account
     */
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "ACCOUNT_UNLOCK", resource = "USER")
    public ResponseEntity<Map<String, String>> unlockUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        try {
            userService.unlockUser(id, httpRequest);
            return ResponseEntity.ok(Map.of("message", "User account unlocked successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUDITOR')")
    @Auditable(action = "USER_STATISTICS", resource = "USER")
    public ResponseEntity<UserStatistics> getUserStatistics() {
        UserStatistics statistics = userService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}
