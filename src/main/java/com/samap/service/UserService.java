package com.samap.service;

import com.samap.model.AuditLog;
import com.samap.model.Role;
import com.samap.model.User;
import com.samap.repository.RoleRepository;
import com.samap.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * User management service with comprehensive security features
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    /**
     * Create new user
     */
    @Transactional
    public User createUser(CreateUserRequest request, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        // Validate username and email uniqueness
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setMustChangePassword(request.isMustChangePassword());
        user.setCreatedBy(currentUser);
        user.setPasswordChangedAt(LocalDateTime.now());

        // Assign default role if no roles specified
        if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
            Role defaultRole = roleRepository.findByNameIgnoreCase(Role.ROLE_USER)
                    .orElseThrow(() -> new IllegalStateException("Default user role not found"));
            user.getRoles().add(defaultRole);
        } else {
            // Assign specified roles
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
                user.getRoles().add(role);
            }
        }

        User savedUser = userRepository.save(user);

        // Log user creation
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_USER_CREATE,
                "Created user: " + savedUser.getUsername(),
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("User created: {} by {}", savedUser.getUsername(), currentUser);
        return savedUser;
    }

    /**
     * Update user
     */
    @Transactional
    public User updateUser(Long userId, UpdateUserRequest request, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String originalData = user.toString();

        // Update basic information
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getAccountStatus() != null) {
            user.setAccountStatus(request.getAccountStatus());
        }

        user.setUpdatedBy(currentUser);

        User savedUser = userRepository.save(user);

        // Log user update
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_USER_UPDATE,
                "Updated user: " + savedUser.getUsername(),
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("User updated: {} by {}", savedUser.getUsername(), currentUser);
        return savedUser;
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long userId, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String username = user.getUsername();
        userRepository.delete(user);

        // Log user deletion
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_USER_DELETE,
                "Deleted user: " + username,
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("User deleted: {} by {}", username, currentUser);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable);
    }

    /**
     * Assign role to user
     */
    @Transactional
    public void assignRole(Long userId, Long roleId, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (user.getRoles().contains(role)) {
            throw new IllegalArgumentException("User already has this role");
        }

        user.getRoles().add(role);
        userRepository.save(user);

        // Log role assignment
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_ROLE_ASSIGN,
                "Assigned role " + role.getName() + " to user " + user.getUsername(),
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("Role {} assigned to user {} by {}", role.getName(), user.getUsername(), currentUser);
    }

    /**
     * Revoke role from user
     */
    @Transactional
    public void revokeRole(Long userId, Long roleId, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        if (!user.getRoles().contains(role)) {
            throw new IllegalArgumentException("User does not have this role");
        }

        user.getRoles().remove(role);
        userRepository.save(user);

        // Log role revocation
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_ROLE_REVOKE,
                "Revoked role " + role.getName() + " from user " + user.getUsername(),
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("Role {} revoked from user {} by {}", role.getName(), user.getUsername(), currentUser);
    }

    /**
     * Change user password
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password if not admin
        if (!isCurrentUserAdmin() && !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);
        user.setUpdatedBy(currentUser);

        userRepository.save(user);

        // Log password change
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_PASSWORD_CHANGE,
                "Password changed for user: " + user.getUsername(),
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("Password changed for user: {} by {}", user.getUsername(), currentUser);
    }

    /**
     * Lock user account
     */
    @Transactional
    public void lockUser(Long userId, int lockoutMinutes, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.lockAccount(lockoutMinutes);
        user.setUpdatedBy(currentUser);
        userRepository.save(user);

        // Log account lock
        auditService.logSecurityEvent(currentUser, AuditLog.ACTION_ACCOUNT_LOCKED,
                "Locked user account: " + user.getUsername() + " for " + lockoutMinutes + " minutes",
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("User account locked: {} by {} for {} minutes", user.getUsername(), currentUser, lockoutMinutes);
    }

    /**
     * Unlock user account
     */
    @Transactional
    public void unlockUser(Long userId, HttpServletRequest httpRequest) {
        String currentUser = getCurrentUsername();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.resetFailedLoginAttempts();
        user.setUpdatedBy(currentUser);
        userRepository.save(user);

        // Log account unlock
        auditService.logSecurityEvent(currentUser, "ACCOUNT_UNLOCKED",
                "Unlocked user account: " + user.getUsername(),
                getClientIpAddress(httpRequest),
                httpRequest.getHeader("User-Agent"),
                AuditLog.ActionStatus.SUCCESS);

        log.info("User account unlocked: {} by {}", user.getUsername(), currentUser);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inactiveDate = now.minusDays(30);
        
        Object[] stats = userRepository.getUserStatistics(now, inactiveDate);
        
        return UserStatistics.builder()
                .totalUsers(((Number) stats[0]).longValue())
                .activeUsers(((Number) stats[1]).longValue())
                .lockedUsers(((Number) stats[2]).longValue())
                .inactiveUsers(((Number) stats[3]).longValue())
                .build();
    }

    // Helper methods
    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals(Role.ROLE_ADMIN));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // DTOs
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private boolean mustChangePassword = false;
        private List<Long> roleIds;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public boolean isMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
        public List<Long> getRoleIds() { return roleIds; }
        public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }
    }

    public static class UpdateUserRequest {
        private String email;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private User.AccountStatus accountStatus;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public User.AccountStatus getAccountStatus() { return accountStatus; }
        public void setAccountStatus(User.AccountStatus accountStatus) { this.accountStatus = accountStatus; }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        // Getters and setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class UserStatistics {
        private long totalUsers;
        private long activeUsers;
        private long lockedUsers;
        private long inactiveUsers;

        public static UserStatisticsBuilder builder() {
            return new UserStatisticsBuilder();
        }

        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
        public long getLockedUsers() { return lockedUsers; }
        public void setLockedUsers(long lockedUsers) { this.lockedUsers = lockedUsers; }
        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }

        public static class UserStatisticsBuilder {
            private long totalUsers;
            private long activeUsers;
            private long lockedUsers;
            private long inactiveUsers;

            public UserStatisticsBuilder totalUsers(long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }

            public UserStatisticsBuilder activeUsers(long activeUsers) {
                this.activeUsers = activeUsers;
                return this;
            }

            public UserStatisticsBuilder lockedUsers(long lockedUsers) {
                this.lockedUsers = lockedUsers;
                return this;
            }

            public UserStatisticsBuilder inactiveUsers(long inactiveUsers) {
                this.inactiveUsers = inactiveUsers;
                return this;
            }

            public UserStatistics build() {
                UserStatistics stats = new UserStatistics();
                stats.totalUsers = this.totalUsers;
                stats.activeUsers = this.activeUsers;
                stats.lockedUsers = this.lockedUsers;
                stats.inactiveUsers = this.inactiveUsers;
                return stats;
            }
        }
    }
}
