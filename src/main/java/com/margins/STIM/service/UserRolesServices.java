/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.SystemUserRoles;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class UserRolesServices {
    
    @PersistenceContext
    private EntityManager em;
    
    public SystemUserRoles createUserRole(SystemUserRoles userRoles) {
        em.persist(userRoles);
        return userRoles;
    }
    
    public SystemUserRoles findUserRoleById(int id) {
        return em.find(SystemUserRoles.class, id);
    }
    
    public SystemUserRoles findUserRoleByName(String roleName) {
        try {
            return em.createQuery("SELECT r FROM SystemUserRoles r WHERE r.userRolename = :roleName", SystemUserRoles.class)
                    .setParameter("roleName", roleName)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public SystemUserRoles updateUserRole(SystemUserRoles userRole) {
        SystemUserRoles existingRole = findUserRoleById(userRole.getId());
        
        if (existingRole != null) {
            existingRole.setUserRolename(userRole.getUserRolename());
            existingRole.setPermissions(userRole.getPermissions());
            em.merge(existingRole);
            return existingRole;
        }
        throw new EntityNotFoundException("User Role not found.");
    }
    
    public void deleteUserRole(SystemUserRoles userRole) {
        SystemUserRoles existingRole = findUserRoleById(userRole.getId());
        if (existingRole != null) {
            existingRole.setDeleted(true);
            em.merge(existingRole);
        } else {
            throw new EntityNotFoundException("User Role not found.");
        }
    }
    
    public List<SystemUserRoles> getAllUserRoles() {
        return em.createQuery("SELECT u FROM SystemUserRoles u", SystemUserRoles.class)
                .getResultList();
    }
    
}
