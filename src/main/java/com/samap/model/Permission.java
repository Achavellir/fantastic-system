package com.samap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission entity for fine-grained access control
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name"),
    @Index(name = "idx_permission_resource", columnList = "resource")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles"})
@ToString(exclude = {"roles"})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Permission name is required")
    @Size(min = 2, max = 100, message = "Permission name must be between 2 and 100 characters")
    private String name;

    @Column(length = 255)
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Resource is required")
    private String resource;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "Action is required")
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    private PermissionType permissionType = PermissionType.FUNCTIONAL;

    @ManyToMany(mappedBy = "permissions")
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public enum PermissionType {
        FUNCTIONAL,  // Business function permissions (CREATE_USER, VIEW_REPORTS)
        DATA,        // Data access permissions (READ_SENSITIVE_DATA)
        SYSTEM       // System-level permissions (SYSTEM_ADMIN, BACKUP_RESTORE)
    }

    // Predefined system permissions
    public static final String USER_CREATE = "USER_CREATE";
    public static final String USER_READ = "USER_READ";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    
    public static final String ROLE_CREATE = "ROLE_CREATE";
    public static final String ROLE_READ = "ROLE_READ";
    public static final String ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ROLE_DELETE = "ROLE_DELETE";
    
    public static final String AUDIT_READ = "AUDIT_READ";
    public static final String AUDIT_EXPORT = "AUDIT_EXPORT";
    
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";
    public static final String SECURITY_MONITOR = "SECURITY_MONITOR";
    
    public static final String DASHBOARD_VIEW = "DASHBOARD_VIEW";
    public static final String REPORTS_VIEW = "REPORTS_VIEW";
    public static final String REPORTS_EXPORT = "REPORTS_EXPORT";
}
