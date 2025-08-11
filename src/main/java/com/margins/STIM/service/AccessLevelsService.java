/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Access_Levels;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class AccessLevelsService {
    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    public Access_Levels createAccessLevel(Access_Levels accessLevel) {
        entityManager.persist(accessLevel);
        return accessLevel;
    }

    public List<Access_Levels> findAllAccessLevels() {
        return entityManager.createQuery("SELECT a FROM Access_Levels a", Access_Levels.class).getResultList();
    }

    public Access_Levels findAccessLevelById(int id) {
        Access_Levels accessLevel = entityManager.find(Access_Levels.class, id);
        if (accessLevel == null) {
            throw new EntityNotFoundException("Access Level with ID " + id + " not found.");
        }
        return accessLevel;
    }

    public Access_Levels updateAccessLevel(int id, Access_Levels updatedAccessLevel) {
        Access_Levels existingAccessLevel = findAccessLevelById(id);
        existingAccessLevel.setLevel_desc(updatedAccessLevel.getLevel_desc());
        entityManager.merge(existingAccessLevel);
        return existingAccessLevel;
    }

    public void deleteAccessLevel(int id) {
        Access_Levels accessLevel = findAccessLevelById(id);
        accessLevel.setDeleted(true);
        entityManager.merge(accessLevel);
    }
}