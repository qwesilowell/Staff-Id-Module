/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.util.RoleCount;
import java.util.List;
import java.util.Set;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class EmployeeRole_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Create a new EmployeeRole.
     *
     * @param employeeRole The EmployeeRole to create.
     * @return The created EmployeeRole.
     */
    public EmployeeRole createEmployeeRole(EmployeeRole employeeRole) {
        entityManager.persist(employeeRole);
        return employeeRole;
    }

    /**
     * Update an existing EmployeeRole.
     *
     * @param roleId The ID of the EmployeeRole to update.
     * @param updatedRole The updated EmployeeRole data.
     * @return The updated EmployeeRole.
     * @throws EntityNotFoundException if the EmployeeRole with the given ID is not found.
     */
    public EmployeeRole updateEmployeeRole(int roleId, EmployeeRole updatedRole) {
        EmployeeRole existingRole = entityManager.find(EmployeeRole.class, roleId);
        if (existingRole != null) {
            existingRole.setRoleName(updatedRole.getRoleName());
            existingRole.setRoleDescription(updatedRole.getRoleDescription());
            entityManager.merge(existingRole);
            return existingRole;
        }
        throw new EntityNotFoundException("EmployeeRole with ID " + roleId + " not found.");
    }

    /**
     * Find an EmployeeRole by ID.
     *
     * @param roleId The ID of the EmployeeRole to find.
     * @return The found EmployeeRole or null if not found.
     */
    public EmployeeRole findEmployeeRoleById(int roleId) {
        return entityManager.find(EmployeeRole.class, roleId);
    }
    
    public List<EmployeeRole> findEmployeeRolesByIds(Set<Integer> roleIds) {
        return entityManager.createQuery("SELECT r FROM EmployeeRole r WHERE r.id IN :roleIds", EmployeeRole.class)
                .setParameter("roleIds", roleIds)
                .getResultList();
    }

    /**
     * Retrieve all EmployeeRoles.
     *
     * @return A list of all EmployeeRoles.
     */
    public List<EmployeeRole> findAllEmployeeRoles() {
        return entityManager.createQuery("SELECT r FROM EmployeeRole r", EmployeeRole.class).getResultList();
    }
    
    public List<EmployeeRole> findEmployeeRoleByName(String searchQuery) {
        return entityManager.createQuery(
                "SELECT r FROM EmployeeRole r WHERE LOWER(r.roleName) LIKE LOWER(:searchQuery)", EmployeeRole.class)
                .setParameter("searchQuery", "%" + searchQuery + "%")
                .getResultList();
    }
    
    public EmployeeRole save(EmployeeRole role) {
        return entityManager.merge(role); // Updates existing or merges detached entity
    }
    
    /**
     * Delete an EmployeeRole by ID.
     *
     * @param roleId The ID of the EmployeeRole to delete.
     * @throws EntityNotFoundException if the EmployeeRole with the given ID is not found.
     */
    public void deleteEmployeeRole(int roleId) {
        EmployeeRole employeeRole = entityManager.find(EmployeeRole.class, roleId);
        if (employeeRole != null) {
            entityManager.remove(employeeRole);
        } else {
            throw new EntityNotFoundException("EmployeeRole with ID " + roleId + " not found.");
        }
    }
    
    public int getTotalRoles() {
        Long count = entityManager.createQuery("SELECT COUNT(r) FROM EmployeeRole r", Long.class)
                .getSingleResult();
        return count.intValue();
    }

    public List<RoleCount> getTopRolesByEmployeeCount(int limit) {
        List<Object[]> results = entityManager.createQuery(
                "SELECT r.roleName, COUNT(e) FROM Employee e JOIN e.role r GROUP BY r.roleName ORDER BY COUNT(e) DESC")
                .setMaxResults(limit)
                .getResultList();
        return results.stream()
                .map(obj -> new RoleCount((String) obj[0], ((Long) obj[1]).intValue()))
                .collect(Collectors.toList());
    }
}
