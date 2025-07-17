package com.samap.service;

import com.samap.model.Permission;
import com.samap.model.Role;
import com.samap.model.User;
import com.samap.repository.PermissionRepository;
import com.samap.repository.RoleRepository;
import com.samap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service to initialize default data on application startup
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing default data...");
        
        initializePermissions();
        initializeRoles();
        initializeUsers();
        
        log.info("Default data initialization completed");
    }

    private void initializePermissions() {
        log.info("Initializing permissions...");
        
        List<PermissionData> permissions = Arrays.asList(
            // User permissions
            new PermissionData(Permission.USER_CREATE, "Create users", "USER", "CREATE", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.USER_READ, "View users", "USER", "READ", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.USER_UPDATE, "Update users", "USER", "UPDATE", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.USER_DELETE, "Delete users", "USER", "DELETE", Permission.PermissionType.FUNCTIONAL),
            
            // Role permissions
            new PermissionData(Permission.ROLE_CREATE, "Create roles", "ROLE", "CREATE", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.ROLE_READ, "View roles", "ROLE", "READ", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.ROLE_UPDATE, "Update roles", "ROLE", "UPDATE", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.ROLE_DELETE, "Delete roles", "ROLE", "DELETE", Permission.PermissionType.FUNCTIONAL),
            
            // Audit permissions
            new PermissionData(Permission.AUDIT_READ, "View audit logs", "AUDIT", "READ", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.AUDIT_EXPORT, "Export audit logs", "AUDIT", "EXPORT", Permission.PermissionType.FUNCTIONAL),
            
            // System permissions
            new PermissionData(Permission.SYSTEM_ADMIN, "System administration", "SYSTEM", "ADMIN", Permission.PermissionType.SYSTEM),
            new PermissionData(Permission.SECURITY_MONITOR, "Security monitoring", "SECURITY", "MONITOR", Permission.PermissionType.SYSTEM),
            
            // Dashboard permissions
            new PermissionData(Permission.DASHBOARD_VIEW, "View dashboard", "DASHBOARD", "VIEW", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.REPORTS_VIEW, "View reports", "REPORTS", "VIEW", Permission.PermissionType.FUNCTIONAL),
            new PermissionData(Permission.REPORTS_EXPORT, "Export reports", "REPORTS", "EXPORT", Permission.PermissionType.FUNCTIONAL)
        );

        for (PermissionData permData : permissions) {
            if (!permissionRepository.existsByNameIgnoreCase(permData.name)) {
                Permission permission = new Permission();
                permission.setName(permData.name);
                permission.setDescription(permData.description);
                permission.setResource(permData.resource);
                permission.setAction(permData.action);
                permission.setPermissionType(permData.type);
                permission.setCreatedBy("SYSTEM");
                
                permissionRepository.save(permission);
                log.debug("Created permission: {}", permData.name);
            }
        }
    }

    private void initializeRoles() {
        log.info("Initializing roles...");
        
        // Create ADMIN role
        Role adminRole = createRoleIfNotExists(
            Role.ROLE_ADMIN,
            "System Administrator",
            "Full system access with all permissions",
            Role.RoleType.SYSTEM,
            true
        );
        
        // Assign all permissions to admin
        List<Permission> allPermissions = permissionRepository.findAll();
        adminRole.getPermissions().addAll(allPermissions);
        roleRepository.save(adminRole);

        // Create USER role
        Role userRole = createRoleIfNotExists(
            Role.ROLE_USER,
            "Regular User",
            "Basic user with limited permissions",
            Role.RoleType.SYSTEM,
            true
        );
        
        // Assign basic permissions to user
        addPermissionsToRole(userRole, Arrays.asList(
            Permission.DASHBOARD_VIEW,
            Permission.REPORTS_VIEW
        ));

        // Create AUDITOR role
        Role auditorRole = createRoleIfNotExists(
            Role.ROLE_AUDITOR,
            "Security Auditor",
            "Can view and analyze audit logs",
            Role.RoleType.SYSTEM,
            true
        );
        
        // Assign audit permissions to auditor
        addPermissionsToRole(auditorRole, Arrays.asList(
            Permission.AUDIT_READ,
            Permission.AUDIT_EXPORT,
            Permission.DASHBOARD_VIEW,
            Permission.REPORTS_VIEW,
            Permission.REPORTS_EXPORT,
            Permission.SECURITY_MONITOR
        ));

        // Create SECURITY_OFFICER role
        Role securityOfficerRole = createRoleIfNotExists(
            Role.ROLE_SECURITY_OFFICER,
            "Security Officer",
            "Can monitor security events and manage user access",
            Role.RoleType.SYSTEM,
            true
        );
        
        // Assign security permissions to security officer
        addPermissionsToRole(securityOfficerRole, Arrays.asList(
            Permission.USER_READ,
            Permission.USER_UPDATE,
            Permission.AUDIT_READ,
            Permission.SECURITY_MONITOR,
            Permission.DASHBOARD_VIEW,
            Permission.REPORTS_VIEW
        ));
    }

    private void initializeUsers() {
        log.info("Initializing users...");
        
        // Create default admin user
        if (!userRepository.existsByUsernameIgnoreCase("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@samap.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("System");
            admin.setLastName("Administrator");
            admin.setAccountStatus(User.AccountStatus.ACTIVE);
            admin.setMustChangePassword(true);
            admin.setCreatedBy("SYSTEM");
            admin.setPasswordChangedAt(LocalDateTime.now());
            
            Role adminRole = roleRepository.findByNameIgnoreCase(Role.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            admin.getRoles().add(adminRole);
            
            userRepository.save(admin);
            log.info("Created default admin user");
        }

        // Create default auditor user
        if (!userRepository.existsByUsernameIgnoreCase("auditor")) {
            User auditor = new User();
            auditor.setUsername("auditor");
            auditor.setEmail("auditor@samap.com");
            auditor.setPassword(passwordEncoder.encode("auditor123"));
            auditor.setFirstName("Security");
            auditor.setLastName("Auditor");
            auditor.setAccountStatus(User.AccountStatus.ACTIVE);
            auditor.setMustChangePassword(true);
            auditor.setCreatedBy("SYSTEM");
            auditor.setPasswordChangedAt(LocalDateTime.now());
            
            Role auditorRole = roleRepository.findByNameIgnoreCase(Role.ROLE_AUDITOR)
                    .orElseThrow(() -> new RuntimeException("Auditor role not found"));
            auditor.getRoles().add(auditorRole);
            
            userRepository.save(auditor);
            log.info("Created default auditor user");
        }

        // Create demo user
        if (!userRepository.existsByUsernameIgnoreCase("demo")) {
            User demo = new User();
            demo.setUsername("demo");
            demo.setEmail("demo@samap.com");
            demo.setPassword(passwordEncoder.encode("demo123"));
            demo.setFirstName("Demo");
            demo.setLastName("User");
            demo.setAccountStatus(User.AccountStatus.ACTIVE);
            demo.setMustChangePassword(false);
            demo.setCreatedBy("SYSTEM");
            demo.setPasswordChangedAt(LocalDateTime.now());

            Role userRole = roleRepository.findByNameIgnoreCase(Role.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            demo.getRoles().add(userRole);

            userRepository.save(demo);
            log.info("Created demo user");
        }

        // Create neeraj user
        if (!userRepository.existsByUsernameIgnoreCase("neeraj")) {
            User neeraj = new User();
            neeraj.setUsername("neeraj");
            neeraj.setEmail("neeraj@samap.com");
            neeraj.setPassword(passwordEncoder.encode("password"));
            neeraj.setFirstName("Neeraj");
            neeraj.setLastName("Kumar");
            neeraj.setAccountStatus(User.AccountStatus.ACTIVE);
            neeraj.setMustChangePassword(false);
            neeraj.setCreatedBy("SYSTEM");
            neeraj.setPasswordChangedAt(LocalDateTime.now());

            Role userRole = roleRepository.findByNameIgnoreCase(Role.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("User role not found"));
            neeraj.getRoles().add(userRole);

            userRepository.save(neeraj);
            log.info("Created neeraj user");
        }
    }

    private Role createRoleIfNotExists(String name, String displayName, String description, 
                                     Role.RoleType type, boolean isSystemRole) {
        return roleRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setRoleType(type);
                    role.setIsSystemRole(isSystemRole);
                    role.setCreatedBy("SYSTEM");
                    
                    Role savedRole = roleRepository.save(role);
                    log.debug("Created role: {}", name);
                    return savedRole;
                });
    }

    private void addPermissionsToRole(Role role, List<String> permissionNames) {
        for (String permissionName : permissionNames) {
            permissionRepository.findByNameIgnoreCase(permissionName)
                    .ifPresent(permission -> {
                        if (!role.getPermissions().contains(permission)) {
                            role.getPermissions().add(permission);
                        }
                    });
        }
        roleRepository.save(role);
    }

    // Helper class for permission data
    private static class PermissionData {
        final String name;
        final String description;
        final String resource;
        final String action;
        final Permission.PermissionType type;

        PermissionData(String name, String description, String resource, String action, Permission.PermissionType type) {
            this.name = name;
            this.description = description;
            this.resource = resource;
            this.action = action;
            this.type = type;
        }
    }
}
