package com.samap.repository;

import com.samap.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name (case-insensitive)
     */
    Optional<Role> findByNameIgnoreCase(String name);

    /**
     * Check if role name exists (case-insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find roles by type
     */
    List<Role> findByRoleType(Role.RoleType roleType);

    /**
     * Find system roles
     */
    List<Role> findByIsSystemRoleTrue();

    /**
     * Find custom roles (non-system)
     */
    List<Role> findByIsSystemRoleFalse();

    /**
     * Find roles with specific permission
     */
    @Query("SELECT DISTINCT r FROM Role r JOIN r.permissions p WHERE p.name = :permissionName")
    List<Role> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Search roles by name or description
     */
    @Query("SELECT r FROM Role r WHERE " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Role> searchRoles(@Param("searchTerm") String searchTerm);

    /**
     * Count roles by type
     */
    long countByRoleType(Role.RoleType roleType);

    /**
     * Find roles assigned to users
     */
    @Query("SELECT DISTINCT r FROM Role r WHERE SIZE(r.users) > 0")
    List<Role> findRolesWithUsers();

    /**
     * Find roles not assigned to any user
     */
    @Query("SELECT r FROM Role r WHERE SIZE(r.users) = 0")
    List<Role> findUnassignedRoles();
}
