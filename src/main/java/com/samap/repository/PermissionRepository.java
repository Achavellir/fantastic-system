package com.samap.repository;

import com.samap.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Permission entity
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Find permission by name (case-insensitive)
     */
    Optional<Permission> findByNameIgnoreCase(String name);

    /**
     * Check if permission name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find permissions by resource
     */
    List<Permission> findByResource(String resource);

    /**
     * Find permissions by action
     */
    List<Permission> findByAction(String action);

    /**
     * Find permissions by resource and action
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);

    /**
     * Find permissions by type
     */
    List<Permission> findByPermissionType(Permission.PermissionType permissionType);

    /**
     * Search permissions by name, description, or resource
     */
    @Query("SELECT p FROM Permission p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.resource) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Permission> searchPermissions(@Param("searchTerm") String searchTerm);

    /**
     * Find permissions assigned to roles
     */
    @Query("SELECT DISTINCT p FROM Permission p WHERE SIZE(p.roles) > 0")
    List<Permission> findPermissionsWithRoles();

    /**
     * Find permissions not assigned to any role
     */
    @Query("SELECT p FROM Permission p WHERE SIZE(p.roles) = 0")
    List<Permission> findUnassignedPermissions();

    /**
     * Count permissions by type
     */
    long countByPermissionType(Permission.PermissionType permissionType);

    /**
     * Find all unique resources
     */
    @Query("SELECT DISTINCT p.resource FROM Permission p ORDER BY p.resource")
    List<String> findAllResources();

    /**
     * Find all unique actions
     */
    @Query("SELECT DISTINCT p.action FROM Permission p ORDER BY p.action")
    List<String> findAllActions();
}
