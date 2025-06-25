/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;
import com.margins.STIM.util.RoleCount;
import com.margins.STIM.util.RoleCountDTO;
import jakarta.ejb.EJB;
import java.util.List;
import java.util.Set;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class EmployeeRole_Service {

    @EJB
    private EntrancesService entranceService;

    @Inject
    private TimeAccessRuleService timeAccessService;

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager em;

    /**
     * Create a new EmployeeRole.
     *
     * @param employeeRole The EmployeeRole to create.
     * @return The created EmployeeRole.
     */
    public EmployeeRole createEmployeeRole(EmployeeRole employeeRole) {
        em.persist(employeeRole);
        return employeeRole;
    }

    /**
     * Update an existing EmployeeRole.
     *
     * @param roleId The ID of the EmployeeRole to update.
     * @param updatedRole The updated EmployeeRole data.
     * @return The updated EmployeeRole.
     * @throws EntityNotFoundException if the EmployeeRole with the given ID is
     * not found.
     */
    public EmployeeRole updateEmployeeRole(int roleId, EmployeeRole updatedRole) {
        EmployeeRole existingRole = em.find(EmployeeRole.class, roleId);
        if (existingRole != null) {
            existingRole.setRoleName(updatedRole.getRoleName());
            existingRole.setRoleDescription(updatedRole.getRoleDescription());
            em.merge(existingRole);
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
        return em.find(EmployeeRole.class, roleId);
    }

    public EmployeeRole findRoleWithEmployeesById(int roleId) {
        return em.createQuery(
                "SELECT r FROM EmployeeRole r LEFT JOIN FETCH r.employees WHERE r.id = :id", EmployeeRole.class)
                .setParameter("id", roleId)
                .getSingleResult();
    }

    public List<EmployeeRole> findEmployeeRolesByIds(Set<Integer> roleIds) {
        return em.createQuery("SELECT r FROM EmployeeRole r WHERE r.id IN :roleIds", EmployeeRole.class)
                .setParameter("roleIds", roleIds)
                .getResultList();
    }

    /**
     * Retrieve all EmployeeRoles.
     *
     * @return A list of all EmployeeRoles.
     */
    public List<EmployeeRole> findAllEmployeeRoles() {
        return em.createQuery("SELECT r FROM EmployeeRole r", EmployeeRole.class).getResultList();
    }

    public List<EmployeeRole> findEmployeeRoleByName(String searchQuery) {
        return em.createQuery(
                "SELECT r FROM EmployeeRole r WHERE LOWER(r.roleName) LIKE LOWER(:searchQuery)", EmployeeRole.class)
                .setParameter("searchQuery", "%" + searchQuery + "%")
                .getResultList();
    }

    public EmployeeRole save(EmployeeRole role) {
        return em.merge(role); // Updates existing or merges detached entity
    }

    /**
     * Delete an EmployeeRole by ID.
     *
     * @param roleId The ID of the EmployeeRole to delete.
     * @throws EntityNotFoundException if the EmployeeRole with the given ID is
     * not found.
     */
    public void deleteEmployeeRole(int roleId) {
        EmployeeRole employeeRole = em.find(EmployeeRole.class, roleId);
        if (employeeRole != null) {
            em.remove(employeeRole);
        } else {
            throw new EntityNotFoundException("EmployeeRole with ID " + roleId + " not found.");
        }
    }

    public int getTotalRoles() {
        Long count = em.createQuery("SELECT COUNT(r) FROM EmployeeRole r", Long.class)
                .getSingleResult();
        return count.intValue();
    }

    public List<RoleCount> getTopRolesByEmployeeCount(int limit) {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT r.roleName, COUNT(e) FROM Employee e JOIN e.role r GROUP BY r.roleName ORDER BY COUNT(e) DESC",
                Object[].class);
        List<Object[]> results = query.setMaxResults(limit).getResultList();
        return results.stream()
                .map(obj -> new RoleCount((String) obj[0], ((Long) obj[1]).intValue()))
                .collect(Collectors.toList());
    }

    public List<RoleCount> getAllRolesWithEmployeeCount() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT r.roleName, COUNT(e) "
                + "FROM Employee e JOIN e.role r "
                + "GROUP BY r.roleName "
                + "ORDER BY COUNT(e) DESC",
                Object[].class
        );

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(obj -> new RoleCount((String) obj[0], ((Long) obj[1]).intValue()))
                .collect(Collectors.toList());
    }

    public void removeRoleAccessWithTimeRules(int roleId, String entranceId) {
        EmployeeRole role = findEmployeeRoleById(roleId);
        Entrances entrance = entranceService.findEntranceById(entranceId);

        if (role != null && entrance != null) {
            // Step 1: Delete time rules
            List<RoleTimeAccess> timeAccessList = timeAccessService.findByRoleAndEntrance(role, entrance);
            System.out.println("removing time access for role>>>>> " + role + "and entrance>>>> " + entrance.getEntrance_Name());

            for (RoleTimeAccess rta : timeAccessList) {
                System.out.println("Deleting Time Rule → Role: " + rta.getEmployeeRole().getRoleName()
                        + ", Entrance: " + rta.getEntrances().getEntrance_Device_ID()
                        + ", Day: " + rta.getDayOfWeek()
                        + ", Start: " + rta.getStartTime()
                        + ", End: " + rta.getEndTime());
                em.remove(rta);
            }

            // Step 2: Remove role-entrance access (ManyToMany)
            role.getAccessibleEntrances().remove(entrance);
            entrance.getAllowedRoles().remove(role);

            em.merge(role);
            em.merge(entrance);
        } else {
            System.out.println("Role or Entrance not found.");
        }
    }

//    
//    public List<RoleCountDTO> getAllRolesWithEmployeeCount() {
//        TypedQuery<Object[]> query = em.createQuery(
//                "SELECT r, COUNT(e) FROM EmployeeRole r LEFT JOIN r.employees e GROUP BY r ORDER BY COUNT(e) DESC",
//                Object[].class
//        );
//
//        List<Object[]> results = query.getResultList();
//
//        return results.stream()
//                .map(obj -> new RoleCountDTO((EmployeeRole) obj[0], ((Long) obj[1]).intValue()))
//                .collect(Collectors.toList());
//    }
    public List<RoleCountDTO> getAllRolesWithCounts() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT r, COUNT(DISTINCT emp), COUNT(DISTINCT ent) "
                + "FROM EmployeeRole r "
                + "LEFT JOIN r.employees emp "
                + "LEFT JOIN r.accessibleEntrances ent "
                + "GROUP BY r "
                + "ORDER BY COUNT(emp) DESC",
                Object[].class
        );

        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(obj -> new RoleCountDTO(
                (EmployeeRole) obj[0],
                ((Long) obj[1]).intValue(), // employee count
                ((Long) obj[2]).intValue() // entrance count
        ))
                .collect(Collectors.toList());
    }
}
